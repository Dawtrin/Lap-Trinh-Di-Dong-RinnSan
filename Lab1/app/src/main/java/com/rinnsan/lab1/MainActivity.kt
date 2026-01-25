package com.rinnsan.lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.random.Random

abstract class DoAnSang {
    abstract val tenMon: String
    abstract fun tinhGiaTien(): Double
}
class BanhMi(val soTrung: Int) : DoAnSang() {
    override val tenMon = "Banh mi Op la"
    override fun tinhGiaTien() = 15.0 + (soTrung * 5.0)
}
class BanhBao(val banKinh: Double) : DoAnSang() {
    override val tenMon = "Banh bao trung cut"
    override fun tinhGiaTien() = PI * banKinh * banKinh * 0.5
}

class LopHocPhan {
    companion object {
        const val TEN_TRUONG = "Dai hoc VKU"
        const val SI_SO_TOI_DA = 40
    }
    lateinit var tenGiangVien: String
    private var _danhSachSinhVien = mutableListOf<String>()

    val danhSachSinhVien: List<String> get() = _danhSachSinhVien

    val maLopHoc: String by lazy {
        "VKU_2024_" + (100..999).random()
    }

    fun themSinhVien(ten: String): String {
        return if (_danhSachSinhVien.size < SI_SO_TOI_DA) {
            _danhSachSinhVien.add(ten)
            "Da them: $ten"
        } else {
            "Lop da day!"
        }
    }
}

object ServerVKU {
    fun layTrangThaiServer(): String = "Server Online - San sang tai tai lieu"
}
enum class HuongDi { BAC, NAM, TAY, DONG }

suspend fun taiTaiLieu(tenFile: String): String {
    delay(1000L)
    return "Noi dung: $tenFile"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UngDungLab1() }
    }
}

@Composable
fun UngDungLab1() {
    var manHinh by remember { mutableIntStateOf(0) }
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }

    fun p(txt: String) { logs = logs + txt }
    fun clear() { logs = emptyList() }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            if (manHinh == 0) {
                Text(
                    text = "Lab 1",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 30.dp)
                )

                NutBam("Bài 1") { manHinh=1; clear(); xuLyBai1(::p) }
                NutBam("Bài 2") { manHinh=2; clear(); xuLyBai2(::p) }
                NutBam("Bài 3") { manHinh=3; clear(); xuLyBai3(::p) }
                NutBam("Bài 4") { manHinh=4; clear(); xuLyBai4(::p) }
            } else {
                Button(
                    onClick = { manHinh = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("<< Quay lại", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(15.dp))
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black).padding(12.dp)) {
                    items(logs) { Text(it, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 16.sp) }
                }
            }
        }
    }
}

@Composable
fun NutBam(t: String, c: ()->Unit) {
    Button(
        onClick = c,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(60.dp)
    ) {
        Text(text = t, fontSize = 20.sp)
    }
}

fun xuLyBai1(p: (String) -> Unit) {
    fun inDuongKe(kyTu: String, soLan: Int) {
        var line = ""
        repeat(soLan) { line += kyTu }
        p(line)
    }

    fun laySoNgauNhien(): Int {
        return Random.nextInt(1, 100)
    }

    p("Hello Dat! Chao mung quay lai voi Kotlin")
    val ten = "Dat"; val tuoi = 19; var soDu = 50
    p("Toi ten la $ten, $tuoi tuoi")
    p("So du: $soDu k")
    soDu += 100
    p("Me chuyen tien -> So du moi: $soDu k")

    val soA = 10; val soB = 5
    p("10 + 5 = ${soA + soB}")
    p("10 > 5 la: ${soA > soB}")

    inDuongKe("=", 30)
    val soMayMan = laySoNgauNhien()
    p("Con so may man hom nay: $soMayMan")
    inDuongKe("-", 30)
}

fun xuLyBai2(p: (String) -> Unit) {
    fun chuanBiDoDung(vararg dungCu: String) {
        p("Sang nay di hoc can mang theo:")
        for (item in dungCu) {
            p("- $item")
        }
    }

    val thu = (2..8).random()
    val lich = when(thu) {
        2 -> "Mang may tinh"; 3 -> "Lap trinh Mobile"
        else -> "Nghi ngoi"
    }
    p("Thu $thu: $lich")
    p("-------------------------")

    val games = mutableListOf("Lien Minh", "FIFA", "DLS")
    games.add("Genshin Impact")
    games[0] = "LMHT (Rank)"
    games.remove("DLS")

    p("Danh sach dao nguoc: ${games.reversed()}")

    var index = 0
    while (index < games.size) {
        val tenGame = games[index]
        p("Game $index: $tenGame - Do dai: ${tenGame.length}")
        index++
    }
    p("-------------------------")

    val mon1 = BanhMi(2)
    val mon2 = BanhBao(3.0)

    with(mon1) {
        p("Mon: $tenMon - Gia: ${tinhGiaTien()}k")
    }
    p("Mon: ${mon2.tenMon} - Gia: %.2fk".format(mon2.tinhGiaTien()))

    p("-------------------------")
    chuanBiDoDung("But", "Vo", "Laptop Dell", "Chuot")
}

fun xuLyBai3(p: (String) -> Unit) {
    p("=== RANDOM QUA TANG ===")
    val qua = listOf("Balo", "Chuot", "Ban phim", "Tai nghe")
    val quaRandom = qua.filter { it.startsWith("B") }.shuffled().take(1)
    p("Qua tang random: $quaRandom")

    p("-------------------------")
    val doiDiemHe4: (Double) -> Double = { d ->
        if(d>=8.5) 4.0 else if(d>=7.0) 3.0 else 2.0
    }
    p("Diem Mobile (he 4): ${doiDiemHe4(9.0)}")

    p("-------------------------")
    p("=== THONG TIN LOP HOC ===")

    val lopAndroid = LopHocPhan().apply {
        themSinhVien("Dat")
        themSinhVien("Minh")
        themSinhVien("Hung")
        tenGiangVien = "Thay Vu"
    }

    p("Ma lop: ${lopAndroid.maLopHoc}")
    p("DS Sinh vien: ${lopAndroid.danhSachSinhVien}")
}

fun xuLyBai4(p: (String) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        p("=== OBJECT & ENUM ===")
        p(ServerVKU.layTrangThaiServer())

        val huong = HuongDi.NAM
        val tb = when(huong) {
            HuongDi.NAM -> "Ve KTX"
            else -> "Ra cong"
        }
        p("Robot: $tb")

        p("\n=== COROUTINE & JOB ===")
        val job = launch {
            p("...Dang tai file nang (5GB)...")
            delay(3000)
            p("Tai xong file nang!")
        }
        delay(1000)
        p("Lau qua, huy download!")
        job.cancel()
        p("Da huy tac vu.")

        p("\n=== ASYNC & AWAIT ===")
        val start = System.currentTimeMillis()
        val t1 = async { taiTaiLieu("Android.pdf") }
        val t2 = async { taiTaiLieu("MangMT.docx") }

        p("Dang tai 2 file song song...")
        p("Xong: ${t1.await()}")
        p("Xong: ${t2.await()}")
        p("Tong thoi gian: ${System.currentTimeMillis() - start}ms")
    }
}