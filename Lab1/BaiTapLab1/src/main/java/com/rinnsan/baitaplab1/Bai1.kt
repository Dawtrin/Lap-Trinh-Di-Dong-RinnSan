package com.rinnsan.baitaplab1

fun main() {
    println("Hello Dat! Chao mung quay lai voi Kotlin")

    val ten = "Dat"
    val tuoi = 19
    var soDuTaiKhoan = 50

    println("Toi ten la $ten")
    println("Nam nay toi $tuoi tuoi")
    println("So du hien tai: $soDuTaiKhoan k")

    soDuTaiKhoan = soDuTaiKhoan + 100
    println("Me vua chuyen tien, so du moi la: $soDuTaiKhoan k")

    val soA = 10
    val soB = 5
    println("Phep cong 10 + 5 = ${soA + soB}")
    println("Phep nhan 10 * 5 = ${soA * soB}")

    val kiemTra = soA > soB
    println("10 lon hon 5 dung khong: $kiemTra")

    inDuongKe("=", 30)

    val soMayMan = laySoNgauNhien()
    println("Con so may man hom nay cua Dat la: $soMayMan")

    inDuongKe("-", 30)
}

fun inDuongKe(kyTu: String, soLan: Int) {
    repeat(soLan) {
        print(kyTu)
    }
    println()
}

fun laySoNgauNhien(): Int {
    return (1..100).random()
}