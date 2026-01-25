package com.rinnsan.baitaplab1


import kotlin.math.PI

abstract class DoAnSang {
    abstract val tenMon: String
    abstract fun tinhGiaTien(): Double
}

class BanhMi(val soTrung: Int) : DoAnSang() {
    override val tenMon = "Banh mi Op la"

    override fun tinhGiaTien(): Double {
        return 15.0 + (soTrung * 5.0)
    }
}

class BanhBao(val banKinh: Double) : DoAnSang() {
    override val tenMon = "Banh bao trung cut"

    override fun tinhGiaTien(): Double {
        return PI * banKinh * banKinh * 0.5
    }
}

fun chuanBiDoDung(vararg dungCu: String) {
    println("Sang nay di hoc can mang theo:")
    for (item in dungCu) {
        println("- $item")
    }
}

fun main() {
    val diemThi = 8.5
    println("Diem tong ket cua Dat: $diemThi")

    if (diemThi >= 9.0) {
        println("Ket qua: Xuat sac - Nhan hoc bong")
    } else if (diemThi >= 7.0) {
        println("Ket qua: Kha Gioi - Dat yeu cau")
    } else {
        println("Ket qua: Can co gang hon o ky sau")
    }

    println("-------------------------")

    val thuTrongTuan = 3
    val lichHoc = when (thuTrongTuan) {
        2 -> "Mang may tinh"
        3 -> "Lap trinh di dong"
        4 -> "GDTC 3/4"
        5 -> "Cong nghe phan mem"
        6, 7 -> "Vi dieu khien"
        else -> "O ktx va ngu"
    }
    println("Lich trinh hom nay (Thu $thuTrongTuan): $lichHoc")

    println("-------------------------")

    val danhSachGame = mutableListOf("Lien Minh", "FIFA Online", "DLS")

    danhSachGame.add("Genshin Impact")
    danhSachGame[0] = "LMHT (Rank)"

    danhSachGame.remove("DLS")

    println("Danh sach game da sap xep lai (Dao nguoc):")
    println(danhSachGame.reversed())

    var index = 0
    while (index < danhSachGame.size) {
        val tenGame = danhSachGame[index]
        println("Game $index: $tenGame - Do dai ten: ${tenGame.length} ky tu")
        index++
    }

    println("-------------------------")

    val buaSang1 = BanhMi(soTrung = 2)
    val buaSang2 = BanhBao(banKinh = 3.0)

    println("--- Hoa don an sang ---")
    with(buaSang1) {
        println("Mon: $tenMon")
        println("Gia tien: ${tinhGiaTien()} k")
    }

    with(buaSang2) {
        println("Mon: $tenMon (Hinh tron)")
        println("Gia tien (tinh theo dien tich): %.2f k".format(tinhGiaTien()))
    }

    var tienTrongVi = 200.0
    tienTrongVi -= buaSang1.tinhGiaTien()
    println("So tien con lai sau khi an sang: $tienTrongVi k")

    println("-------------------------")
    chuanBiDoDung("Laptop", "Sac pin", "Chuot", "Tai nghe", "Binh nuoc")
}