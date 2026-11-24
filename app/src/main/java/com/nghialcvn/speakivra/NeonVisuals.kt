package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Approved cyan/blue/purple deep-glass visual language shared by all Neon screens. */
internal object NeonLook {
    val cyan = Color(0xFF00E7F9)
    val azure = Color(0xFF058BFF)
    val blue = Color(0xFF3055F3)
    val violet = Color(0xFF9E35F4)
    val lilac = Color(0xFFCD78FF)
    val panel = Color(0xEB102452)
    val deep = Color(0xF0122145)
    val text = Color(0xFFF6F8FF)
    val muted = Color(0xFFB6C5E7)
    val crest = Brush.horizontalGradient(listOf(cyan, azure, violet))
    val glossyPanel = Brush.linearGradient(listOf(
        Color(0xEF193B71), Color(0xF0162759), Color(0xF2191F55)
    ))
}

/** Use on a single entire card; no geometry changes to the approved layout. */
internal fun Modifier.neonGlass(radius: Int = 18, strong: Boolean = false): Modifier {
    val shape = RoundedCornerShape(radius.dp)
    return this.shadow(
        if (strong) 13.dp else 7.dp, shape,
        ambientColor = Color(0xAA0C5DE4),
        spotColor = if (strong) NeonLook.violet else NeonLook.azure
    ).clip(shape)
        .background(NeonLook.glossyPanel)
        .border(if (strong) 1.7.dp else 1.1.dp, NeonLook.crest, shape)
}

/** Transparent speech symbol is painted natively. No white-square icon overlay. */
@Composable
internal fun NeonBrandMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val speech = Path().apply {
            moveTo(w*.21f,h*.2f)
            cubicTo(w*.38f,h*.04f,w*.79f,h*.04f,w*.88f,h*.32f)
            cubicTo(w*.98f,h*.63f,w*.68f,h*.86f,w*.42f,h*.76f)
            lineTo(w*.14f,h*.97f)
            lineTo(w*.19f,h*.68f)
            cubicTo(w*.02f,h*.52f,w*.08f,h*.31f,w*.21f,h*.2f)
            close()
        }
        drawPath(speech,brush=Brush.linearGradient(
            listOf(NeonLook.cyan,NeonLook.blue,NeonLook.violet)))
        drawPath(speech,color=NeonLook.lilac.copy(alpha=.65f),
            style=Stroke(width=w*.015f))
        drawCircle(brush=Brush.radialGradient(listOf(Color(0xCC152C97),
            Color(0xFF0B1D4A))),radius=w*.28f,center=Offset(w*.52f,h*.43f))
        val bars=listOf(.28f,.51f,.76f,.43f,.64f,.3f)
        bars.forEachIndexed{ i,v ->
            val x=w*(.38f + i*.056f)
            drawLine(Color.White,Offset(x,h*(.43f-v*.17f)),
                Offset(x,h*(.43f+v*.17f)),strokeWidth=w*.025f,
                cap=StrokeCap.Round)
        }
        drawCircle(NeonLook.lilac, radius=w*.034f,
            center=Offset(w*.87f,h*.12f))
        drawLine(NeonLook.cyan,Offset(w*.87f,h*.02f),Offset(w*.87f,h*.2f),
            strokeWidth=w*.016f,cap=StrokeCap.Round)
        drawLine(NeonLook.cyan,Offset(w*.78f,h*.12f),Offset(w*.96f,h*.12f),
            strokeWidth=w*.016f,cap=StrokeCap.Round)
    }
}

/** Sculpted headphones artwork without a pasted square or external image. */
@Composable
internal fun NeonHeadphones(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w=size.width; val h=size.height
        val arch=Path().apply {
            moveTo(w*.2f,h*.64f)
            cubicTo(w*.11f,h*.04f,w*.83f,-h*.02f,w*.83f,h*.64f)
        }
        drawPath(arch,brush=Brush.linearGradient(
            listOf(NeonLook.cyan,NeonLook.azure,NeonLook.lilac)),
            style=Stroke(width=w*.16f,cap=StrokeCap.Round))
        drawPath(arch,color=Color(0xFFA6E7FF),
            style=Stroke(width=w*.022f,cap=StrokeCap.Round))
        fun ear(x:Float,cy:Float){
            drawRoundRect(
                brush=Brush.linearGradient(listOf(NeonLook.cyan,NeonLook.blue,NeonLook.violet)),
                topLeft=Offset(x-w*.15f,cy-h*.19f),
                size=Size(w*.27f,h*.39f),
                cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.095f))
            drawRoundRect(brush=Brush.radialGradient(listOf(Color(0xFF0C2452),
                Color(0xFF3D48AD))),topLeft=Offset(x-w*.11f,cy-h*.13f),
                size=Size(w*.18f,h*.28f),
                cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.072f))
            drawRoundRect(color=Color(0xAA96EEFF),
                topLeft=Offset(x-w*.15f,cy-h*.19f),
                size=Size(w*.27f,h*.39f),
                cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.095f),
                style=Stroke(width=w*.016f))
        }
        ear(w*.23f,h*.69f);ear(w*.83f,h*.69f)
        val bars=listOf(.36f,.72f,1f,.65f,.35f)
        bars.forEachIndexed{i,v->
            val x=w*(.41f+i*.045f)
            drawLine(NeonLook.cyan,Offset(x,h*(.54f-v*.09f)),
                Offset(x,h*(.54f+v*.09f)),strokeWidth=w*.022f,
                cap=StrokeCap.Round)
        }
    }
}

/** Metallic studio microphone pictured in the approved five-tab reference. */
@Composable
internal fun NeonVintageTabMicrophone(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w=size.width;val h=size.height
        val center=Offset(w*.5f,h*.43f)
        drawCircle(brush=Brush.radialGradient(listOf(
            Color(0x8854D4FF),Color(0x115C72FF),Color.Transparent),
            center=center,radius=w*.48f),radius=w*.5f,center=center)
        val bodyLeft=w*.31f
        val top=h*.06f
        val bh=h*.61f
        drawRoundRect(brush=Brush.horizontalGradient(listOf(
            Color(0xFF40536F),Color(0xFFFFFFFF),Color(0xFFB8C1D5),
            Color(0xFFF9F9FB),Color(0xFF526073))),
            topLeft=Offset(bodyLeft,top),size=Size(w*.38f,bh),
            cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.16f))
        listOf(.17f,.26f,.35f,.44f,.53f).forEach{ y->
            drawLine(Color(0xFF677184),Offset(bodyLeft+w*.04f,h*y),
                Offset(bodyLeft+w*.34f,h*y),strokeWidth=w*.035f,
                cap=StrokeCap.Round)
        }
        drawLine(Color(0xFFF0F4FC),Offset(w*.5f,h*.67f),
            Offset(w*.5f,h*.87f),strokeWidth=w*.07f,cap=StrokeCap.Round)
        drawLine(Color(0xFFDCE7F6),Offset(w*.28f,h*.90f),
            Offset(w*.72f,h*.90f),strokeWidth=w*.075f,cap=StrokeCap.Round)
    }
}
