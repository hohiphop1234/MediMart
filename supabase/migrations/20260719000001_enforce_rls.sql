-- One-time hardening migration for a fresh MediMart Supabase project.
-- It removes every existing policy on MediMart tables, then recreates the
-- intended least-privilege policies. It does not delete application data.

do $$
declare
  v_policy record;
begin
  for v_policy in
    select tablename, policyname
    from pg_policies
    where schemaname = 'public'
      and tablename = any (array[
        'profiles', 'categories', 'products', 'banners', 'addresses',
        'orders', 'order_items', 'point_transactions', 'reward_redemptions'
      ])
  loop
    execute format('drop policy if exists %I on public.%I', v_policy.policyname, v_policy.tablename);
  end loop;
end;
$$;

alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.products enable row level security;
alter table public.banners enable row level security;
alter table public.addresses enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;
alter table public.point_transactions enable row level security;
alter table public.reward_redemptions enable row level security;

revoke all on table public.profiles from anon, authenticated;
revoke all on table public.categories from anon, authenticated;
revoke all on table public.products from anon, authenticated;
revoke all on table public.banners from anon, authenticated;
revoke all on table public.addresses from anon, authenticated;
revoke all on table public.orders from anon, authenticated;
revoke all on table public.order_items from anon, authenticated;
revoke all on table public.point_transactions from anon, authenticated;
revoke all on table public.reward_redemptions from anon, authenticated;

grant select on public.categories, public.products, public.banners to anon, authenticated;
grant select on public.profiles, public.addresses, public.orders, public.order_items,
  public.point_transactions, public.reward_redemptions to authenticated;
grant insert, update, delete on public.addresses to authenticated;

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
