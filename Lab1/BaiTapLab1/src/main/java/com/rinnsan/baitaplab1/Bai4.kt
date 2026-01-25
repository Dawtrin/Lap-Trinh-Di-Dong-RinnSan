package com.rinnsan.baitaplab1



import kotlinx.coroutines.*
import java.lang.Exception

object ServerVKU {
    fun layTrangThaiServer(): String {
        return "Server Online - San sang tai tai lieu"
    }
}

enum class HuongDi {
    BAC, NAM, TAY, DONG
}

suspend fun taiTaiLieu(tenFile: String): String {
    delay(1000L)
    return "Noi dung file $tenFile"
}

suspend fun xuLyDuLieu() {
    println("Dang xu ly du lieu...")
    val duLieu = taiTaiLieu("Diem_Thi_TANC.xlsx")
    println("Da xu ly xong: $duLieu")
}

fun main() = runBlocking {
    println("=== OBJECT & ENUM ===")

    println(ServerVKU.layTrangThaiServer())

    val huongRobot = HuongDi.BAC
    val thongBao = when (huongRobot) {
        HuongDi.BAC -> "Robot di ve phia Giang duong Khu V"
        HuongDi.NAM -> "Robot di ve phia KTX"
        HuongDi.TAY -> "Robot di ve phia Nha xe"
        HuongDi.DONG -> "Robot di ra Cong chinh"
    }
    println("Trang thai Robot: $thongBao")


    println("\n=== COROUTINE & JOB (HUY TAC VU) ===")

    val job: Job = GlobalScope.launch {
        println("...Dang thu tai file nang (5GB)...")
        delay(3000L)
        println("Tai xong file nang!")
    }

    delay(1000L)
    println("Thay lau qua, huy download!")
    job.cancel()
    println("Da huy tac vu tai file nang.")


    println("\n=== ASYNC & AWAIT (BAT DONG BO) ===")

    val thoiGianBatDau = System.currentTimeMillis()

    val duLieu1 = async { taiTaiLieu("Giao_trinh_Android.pdf") }
    val duLieu2 = async { taiTaiLieu("De_cuong_Mang_may_tinh.docx") }

    println("Dang tai 2 file cung luc...")

    val ketQua1 = duLieu1.await()
    val ketQua2 = duLieu2.await()

    println("Tai xong: $ketQua1")
    println("Tai xong: $ketQua2")

    val tongThoiGian = System.currentTimeMillis() - thoiGianBatDau
    println("Tong thoi gian tai: $tongThoiGian ms (Nhanh hon chay tuan tu)")


    println("\n=== TRY / CATCH (XU LY LOI) ===")

    try {
        println("Dang ket noi den Cong thong tin sinh vien...")
        moKetNoiServer(trangThai = false)
    } catch (e: Exception) {
        println("LOI: Khong the ket noi! Chi tiet: ${e.message}")
    } finally {
        println("Ket thuc phien lam viec.")
    }
}

fun moKetNoiServer(trangThai: Boolean) {
    if (!trangThai) {
        throw Exception("Server bao tri, vui long quay lai sau.")
    }
}