package com.rinnsan.baitaplab1

class LopHocPhan {
    companion object {
        const val TEN_TRUONG = "Dai hoc VKU"
        const val SI_SO_TOI_DA = 40
    }

    lateinit var tenGiangVien: String

    private var _danhSachSinhVien = mutableListOf<String>()

    val danhSachSinhVien: List<String>
        get() = _danhSachSinhVien

    val maLopHoc: String by lazy {
        println("...Dang tao ma lop...")
        "VKU_2024_" + (100..999).random()
    }

    fun themSinhVien(ten: String) {
        if (_danhSachSinhVien.size < SI_SO_TOI_DA) {
            _danhSachSinhVien.add(ten)
        } else {
            println("Lop da day!")
        }
    }
}

fun main() {
    println("=== QUAN LY MON HOC (SET) ===")

    val monHocCuaDat = mutableSetOf("Mang may tinh", "Lap trinh di dong", "TANC", "Vi dieu khien")
    val monHocCuaBan = setOf("TANC", "Marketing", "Vi dieu khien")

    println("Mon hoc ky nay cua Dat: $monHocCuaDat")

    val hocChung = monHocCuaDat.intersect(monHocCuaBan)
    println("Dat va Ban hoc chung mon: $hocChung")

    val tatCaMon = monHocCuaDat.union(monHocCuaBan)
    println("Tong cac mon cua 2 dua: $tatCaMon")


    println("-------------------------")
    println("=== BANG DIEM (MAP) ===")

    val bangDiem = mutableMapOf<String, Double>(
        "Mang may tinh" to 7.5,
        "Lap trinh di dong" to 9.0,
        "Vi dieu khien" to 8.0
    )

    bangDiem["TANC"] = 8.5
    bangDiem["Mang may tinh"] = 8.0

    println("--- Bang diem chi tiet ---")
    bangDiem.forEach { mon, diem ->
        println("Mon $mon: $diem diem")
    }

    val monDiemCao = bangDiem.filter { it.value >= 8.5 }
        .map { it.key.uppercase() }

    println("Cac mon Dat dat diem Gioi: $monDiemCao")


    println("-------------------------")
    println("=== RANDOM QUA TANG ===")

    val danhSachQua = listOf("Balo VKU", "Chuot", "Ban phim", "Tai nghe", "Sach")

    val quaTang = danhSachQua.filter { it.startsWith("B") }
        .shuffled()
        .take(1)
        .sorted()

    println("Qua tang random hom nay: $quaTang")


    println("-------------------------")
    println("=== TINH DIEM HE 4 (LAMBDA) ===")

    val doiDiemHe4: (Double) -> Double = { diem10 ->
        if (diem10 >= 8.5) 4.0 else if (diem10 >= 7.0) 3.0 else 2.0
    }

    println("Diem Lap trinh di dong (he 4): ${doiDiemHe4(9.0)}")


    println("-------------------------")
    println("=== THONG TIN LOP HOC (CLASS & SCOPE) ===")

    val lopAndroid = LopHocPhan().apply {
        themSinhVien("Dat")
        themSinhVien("Minh")
        themSinhVien("Hung")

        tenGiangVien = "Thay Ngo Le Quan"
    }

    println("Truong: ${LopHocPhan.TEN_TRUONG}")

    with(lopAndroid) {
        println("Giang vien: $tenGiangVien")
        println("Ma lop: $maLopHoc")
        println("Danh sach SV: $danhSachSinhVien")
    }

    var siSoHienTai: Int? = null

    val siSoChuan = siSoHienTai ?: 0
    println("Si so du phong: $siSoChuan")

    siSoHienTai?.let {
        println("Si so khong null, gia tri la: $it")
    }
}