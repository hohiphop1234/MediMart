-- MediMart initial schema.
-- This migration is designed for Supabase Postgres with Row Level Security enabled.

create extension if not exists pgcrypto;

do $$
begin
  create type public.order_status as enum ('PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'CANCELLED');
exception
  when duplicate_object then null;
end $$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default 'Khách hàng' check (char_length(display_name) between 1 and 100),
  phone text,
  avatar_path text,
  loyalty_points integer not null default 0 check (loyalty_points >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.categories (
  id uuid primary key default gen_random_uuid(),
  name text not null check (char_length(name) between 1 and 120),
  icon text,
  product_count integer not null default 0 check (product_count >= 0),
  created_at timestamptz not null default now()
);

create table if not exists public.products (
  id uuid primary key default gen_random_uuid(),
  category_id uuid references public.categories(id) on delete set null,
  name text not null check (char_length(name) between 1 and 255),
  description text,
  price numeric(12, 0) not null check (price >= 0),
  sale_price numeric(12, 0) check (sale_price is null or (sale_price >= 0 and sale_price <= price)),
  unit text,
  image_path text,
  brand text,
  country text,
  is_flash_sale boolean not null default false,
  is_best_seller boolean not null default false,
  is_reward_item boolean not null default false,
  point_price integer check (point_price is null or point_price > 0),
  attributes jsonb not null default '{}'::jsonb,
  flash_sale_ends_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint reward_product_requires_points check (not is_reward_item or point_price is not null)
);

create table if not exists public.banners (
  id uuid primary key default gen_random_uuid(),
  image_path text not null,
  link_to text,
  position integer not null default 0 check (position >= 0),
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.addresses (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  recipient_name text not null check (char_length(recipient_name) between 1 and 100),
  phone text not null check (char_length(phone) between 8 and 20),
  address_line text not null check (char_length(address_line) between 8 and 500),
  is_default boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists addresses_one_default_per_user
  on public.addresses(user_id) where is_default;

create table if not exists public.orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete restrict,
  address_id uuid not null references public.addresses(id) on delete restrict,
  total_amount numeric(12, 0) not null check (total_amount >= 0),
  earned_points integer not null default 0 check (earned_points >= 0),
  status public.order_status not null default 'PENDING',
  payment_method text not null default 'COD' check (payment_method in ('COD')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  product_id uuid not null references public.products(id) on delete restrict,
  product_name text not null,
  quantity integer not null check (quantity > 0),
  unit_price numeric(12, 0) not null check (unit_price >= 0)
);

create table if not exists public.point_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  delta integer not null check (delta <> 0),
  reason text not null check (reason in ('ORDER_EARNED', 'REWARD_REDEEMED', 'ADMIN_ADJUSTMENT')),
  order_id uuid references public.orders(id) on delete set null,
  product_id uuid references public.products(id) on delete set null,
  created_at timestamptz not null default now()
);

create table if not exists public.reward_redemptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  product_id uuid not null references public.products(id) on delete restrict,
  point_cost integer not null check (point_cost > 0),
  created_at timestamptz not null default now()
);

create index if not exists products_category_id_idx on public.products(category_id);
create index if not exists products_flash_sale_idx on public.products(is_flash_sale) where is_flash_sale;
create index if not exists products_best_seller_idx on public.products(is_best_seller) where is_best_seller;
create index if not exists addresses_user_id_idx on public.addresses(user_id);
create index if not exists orders_user_id_created_at_idx on public.orders(user_id, created_at desc);
create index if not exists order_items_order_id_idx on public.order_items(order_id);
create index if not exists point_transactions_user_id_created_at_idx on public.point_transactions(user_id, created_at desc);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, display_name, phone)
  values (
    new.id,
    coalesce(nullif(trim(new.raw_user_meta_data ->> 'display_name'), ''), 'Khách hàng'),
    new.phone
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

drop trigger if exists addresses_set_updated_at on public.addresses;
create trigger addresses_set_updated_at
before update on public.addresses
for each row execute function public.set_updated_at();

drop trigger if exists products_set_updated_at on public.products;
create trigger products_set_updated_at
before update on public.products
for each row execute function public.set_updated_at();

drop trigger if exists orders_set_updated_at on public.orders;
create trigger orders_set_updated_at
before update on public.orders
for each row execute function public.set_updated_at();

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.products enable row level security;
alter table public.banners enable row level security;
alter table public.addresses enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;
alter table public.point_transactions enable row level security;
alter table public.reward_redemptions enable row level security;

create policy "Public catalog is readable"
on public.categories for select to anon, authenticated using (true);

create policy "Public products are readable"
on public.products for select to anon, authenticated using (true);

create policy "Public banners are readable"
on public.banners for select to anon, authenticated using (is_active);

create policy "Users can read their profile"
on public.profiles for select to authenticated using ((select auth.uid()) = id);

create policy "Users can read their addresses"
on public.addresses for select to authenticated using ((select auth.uid()) = user_id);

create policy "Users can add their addresses"
on public.addresses for insert to authenticated with check ((select auth.uid()) = user_id);

create policy "Users can update their addresses"
on public.addresses for update to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "Users can delete their addresses"
on public.addresses for delete to authenticated using ((select auth.uid()) = user_id);

create policy "Users can read their orders"
on public.orders for select to authenticated using ((select auth.uid()) = user_id);

create policy "Users can read their order items"
on public.order_items for select to authenticated
using (
  exists (
    select 1 from public.orders
    where orders.id = order_items.order_id
      and orders.user_id = (select auth.uid())
  )
);

create policy "Users can read their point history"
on public.point_transactions for select to authenticated using ((select auth.uid()) = user_id);

create policy "Users can read their reward redemptions"
on public.reward_redemptions for select to authenticated using ((select auth.uid()) = user_id);

-- Profile updates use a narrow RPC instead of a direct UPDATE policy so a user
-- cannot modify loyalty_points or phone through the mobile client.
create or replace function public.update_my_profile(
  p_display_name text,
  p_avatar_path text default null
)
returns public.profiles
language plpgsql
security definer
set search_path = public
as $$
declare
  v_profile public.profiles;
begin
  if (select auth.uid()) is null then
    raise exception 'Authentication required';
  end if;

  if p_display_name is null or char_length(trim(p_display_name)) not between 1 and 100 then
    raise exception 'Invalid display name';
  end if;

  update public.profiles
  set display_name = trim(p_display_name),
      avatar_path = nullif(trim(coalesce(p_avatar_path, '')), '')
  where id = (select auth.uid())
  returning * into v_profile;

  return v_profile;
end;
$$;

-- Checkout and reward redemption are security-definer functions so clients
-- cannot submit their own price, points, owner id, or order rows.
create or replace function public.checkout(
  p_items jsonb,
  p_address_id uuid,
  p_payment_method text default 'COD'
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_user_id uuid := (select auth.uid());
  v_item_count integer;
  v_matched_count integer;
  v_total numeric(12, 0);
  v_points integer;
  v_order_id uuid;
begin
  if v_user_id is null then
    raise exception 'Authentication required';
  end if;

  if jsonb_typeof(p_items) <> 'array' or jsonb_array_length(p_items) = 0 then
    raise exception 'Cart is empty';
  end if;

  if p_payment_method not in ('COD') then
    raise exception 'Unsupported payment method';
  end if;

  if not exists (
    select 1 from public.addresses
    where id = p_address_id and user_id = v_user_id
  ) then
    raise exception 'Address not found';
  end if;

  select count(*) into v_item_count
  from jsonb_to_recordset(p_items) as item(product_id uuid, quantity integer);

  if exists (
    select 1
    from jsonb_to_recordset(p_items) as item(product_id uuid, quantity integer)
    where item.product_id is null or item.quantity is null or item.quantity <= 0
  ) then
    raise exception 'Invalid cart item';
  end if;

  with requested as (
    select item.product_id, item.quantity
    from jsonb_to_recordset(p_items) as item(product_id uuid, quantity integer)
  ), priced as (
    select requested.product_id, requested.quantity, products.name,
      coalesce(products.sale_price, products.price) as unit_price
    from requested
    join public.products on products.id = requested.product_id
  )
  select count(*), coalesce(sum(unit_price * quantity), 0)
  into v_matched_count, v_total
  from priced;

  if v_matched_count <> v_item_count then
    raise exception 'One or more products do not exist';
  end if;

  v_points := floor(v_total / 10000)::integer;

  insert into public.orders (user_id, address_id, total_amount, earned_points, payment_method)
  values (v_user_id, p_address_id, v_total, v_points, p_payment_method)
  returning id into v_order_id;

  insert into public.order_items (order_id, product_id, product_name, quantity, unit_price)
  select v_order_id, products.id, products.name, requested.quantity,
    coalesce(products.sale_price, products.price)
  from jsonb_to_recordset(p_items) as requested(product_id uuid, quantity integer)
  join public.products on products.id = requested.product_id;

  update public.profiles
  set loyalty_points = loyalty_points + v_points
  where id = v_user_id;

  if v_points > 0 then
    insert into public.point_transactions (user_id, delta, reason, order_id)
    values (v_user_id, v_points, 'ORDER_EARNED', v_order_id);
  end if;

  return jsonb_build_object(
    'order_id', v_order_id,
    'total_amount', v_total,
    'earned_points', v_points,
    'status', 'PENDING'
  );
end;
$$;

create or replace function public.redeem_reward(p_product_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_user_id uuid := (select auth.uid());
  v_points integer;
  v_cost integer;
  v_redemption_id uuid;
begin
  if v_user_id is null then
    raise exception 'Authentication required';
  end if;

  select loyalty_points into v_points
  from public.profiles
  where id = v_user_id
  for update;

  select point_price into v_cost
  from public.products
  where id = p_product_id and is_reward_item
  for update;

  if not found or v_cost is null then
    raise exception 'Reward not found';
  end if;

  if v_points < v_cost then
    raise exception 'Not enough points';
  end if;

  update public.profiles
  set loyalty_points = loyalty_points - v_cost
  where id = v_user_id;

  insert into public.reward_redemptions (user_id, product_id, point_cost)
  values (v_user_id, p_product_id, v_cost)
  returning id into v_redemption_id;

  insert into public.point_transactions (user_id, delta, reason, product_id)
  values (v_user_id, -v_cost, 'REWARD_REDEEMED', p_product_id);

  return jsonb_build_object(
    'redemption_id', v_redemption_id,
    'remaining_points', v_points - v_cost
  );
end;
$$;

revoke all on function public.update_my_profile(text, text) from public;
revoke all on function public.checkout(jsonb, uuid, text) from public;
revoke all on function public.redeem_reward(uuid) from public;
grant execute on function public.update_my_profile(text, text) to authenticated;
grant execute on function public.checkout(jsonb, uuid, text) to authenticated;
grant execute on function public.redeem_reward(uuid) to authenticated;

insert into storage.buckets (id, name, public)
values
  ('product-images', 'product-images', true),
  ('banner-images', 'banner-images', true),
  ('avatars', 'avatars', false)
on conflict (id) do update set public = excluded.public;

create policy "Public catalog images are readable"
on storage.objects for select to anon, authenticated
using (bucket_id in ('product-images', 'banner-images'));

create policy "Users can read their avatar"
on storage.objects for select to authenticated
using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid()::text));

create policy "Users can upload their avatar"
on storage.objects for insert to authenticated
with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid()::text));

create policy "Users can update their avatar"
on storage.objects for update to authenticated
using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid()::text))
with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid()::text));

create policy "Users can delete their avatar"
on storage.objects for delete to authenticated
using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid()::text));
