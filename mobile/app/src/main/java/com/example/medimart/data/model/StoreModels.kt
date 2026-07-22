package com.example.medimart.data.model

data class StoreBranch(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class StoreBranchWithDistance(
    val branch: StoreBranch,
    val distanceKm: Double?
)

object StoreBranchData {
    val branches = listOf(
        StoreBranch("Nhà Thuốc FPT Long Châu Quang Trung", 10.843228972004523, 106.64356733787275),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Văn Quá", 10.853175997810927, 106.63850332735281),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Văn Khối", 10.846263692921386, 106.65051962350185),
        StoreBranch("Nhà Thuốc FPT Long Châu Phan Huy Ích", 10.839014030767917, 106.63893248078668),
        StoreBranch("Nhà Thuốc FPT Long Châu Đông Bắc", 10.859498087907872, 106.63052107348234),
        StoreBranch("Nhà Thuốc FPT Long Châu Phạm Văn Chiêu", 10.851490084527146, 106.65618444882926),
        StoreBranch("Nhà Thuốc FPT Long Châu Phạm Văn Bạch", 10.832270001311414, 106.64708639603069),
        StoreBranch("Nhà Thuốc FPT Long Châu Trần Thị Trọng", 10.828645022709694, 106.63858915803958),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Thị Kiều", 10.871682027495291, 106.64552207221168),
        StoreBranch("Nhà Thuốc FPT Long Châu Trung Mỹ Tây", 10.849412334510758, 106.61833032564556),
        StoreBranch("Nhà Thuốc FPT Long Châu đường số 11", 10.84312556995175, 106.66761556320016),
        StoreBranch("Nhà Thuốc FPT Long Châu Tô Ký", 10.859803699947566, 106.62158600397034),
        StoreBranch("Nhà Thuốc FPT Long Châu Thống Nhất", 10.83435881471209, 106.66874351770592),
        StoreBranch("Nhà Thuốc FPT Long Châu Thái Thị Giữ", 10.84949324648303, 106.61306259603066),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Ảnh Thủ", 10.863991886724794, 106.61589500869435),
        StoreBranch("Nhà Thuốc FPT Long Châu Phan Văn Hùm", 10.857248420652168, 106.62078735784078),
        StoreBranch("Nhà Thuốc FPT Long Châu Dương Thị Giang", 10.82555209213265, 106.62404892409967),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Sỹ Sách", 10.821336901992323, 106.64001343241094),
        StoreBranch("Nhà Thuốc FPT Long Châu Trần Thị Cờ", 10.874284324376328, 106.65628388594725),
        StoreBranch("Nhà Thuốc FPT Long Châu Bùi Văn Ngữ", 10.835484612999519, 106.62005174152488),
        StoreBranch("Nhà Thuốc FPT Long Châu Dương Quảng Hàm", 10.83378862819964, 106.69095137221164),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Thị Búp", 10.877976140036093, 106.63223044732082),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Kiệm", 10.818637841400442, 106.68355256296233),
        StoreBranch("Nhà Thuốc FPT Long Châu Phan Văn Hớn", 10.849588530442592, 106.59388394946453),
        StoreBranch("Nhà Thuốc FPT Long Châu Nguyễn Văn Nghi", 10.825012487476998, 106.69199868015133)
    )
}
