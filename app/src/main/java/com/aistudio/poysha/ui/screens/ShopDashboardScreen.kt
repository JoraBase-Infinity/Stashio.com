package com.aistudio.poysha.ui.screens

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aistudio.poysha.R
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Shop

/* ------------------------------------------------------------------ *
 *  res/font: poppins_light/regular/medium/semibold/bold/extrabold.ttf,
 *            fa_solid_900.ttf (Font Awesome 6.4 Free Solid)
 *  minSdk 28+
 * ------------------------------------------------------------------ */

// ---------- :root variables ----------
private val Primary = Color(0xFF0B4A3E)
private val Secondary = Color(0xFFD4A017)
private val TextDark = Color(0xFF1F2937)
private val TextLight = Color(0xFF6B7280)
private val Danger = Color(0xFFEF4444)
private val Success = Color(0xFF10B981)
private val Warning = Color(0xFFF59E0B)

private val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
)
private val FaSolid = FontFamily(Font(R.font.fa_solid_900, FontWeight.Normal))

// ---------- state ----------
data class ShopItem(
    val id: String,
    val name: String,
    val description: String?,
    val image: String?,
    val role: String,          // "Owner" / "Manager" ...
    val isCreator: Boolean,
)

enum class ShopModalMode { Create, Edit }

class DashboardUiState {
    var userName by mutableStateOf("User")
    var loading by mutableStateOf(false)
    var loadFailed by mutableStateOf(false)
    val shops = mutableStateListOf<ShopItem>()

    // shop modal
    var showShopModal by mutableStateOf(false)
    var modalMode by mutableStateOf(ShopModalMode.Create)
    var editShopId by mutableStateOf("")
    var shopName by mutableStateOf("")
    var shopDesc by mutableStateOf("")
    var shopAddress by mutableStateOf("")
    var logoUrl by mutableStateOf("")
    var uploadText by mutableStateOf("Tap to Upload Shop Logo")
    var uploadTextColor by mutableStateOf(TextLight)
    var modalStatus by mutableStateOf("")
    var modalStatusColor by mutableStateOf(Color.Black)
    var actionBtnEnabled by mutableStateOf(true)
    var deleteBtnEnabled by mutableStateOf(true)

    // otp modal
    var showOtpModal by mutableStateOf(false)
    var otpEmail by mutableStateOf("")
    var otpInput by mutableStateOf("")
    var otpStatus by mutableStateOf("")
    var otpStatusColor by mutableStateOf(Color.Black)
    var confirmDeleteEnabled by mutableStateOf(true)

    fun setModalStatus(text: String, color: Color) { modalStatus = text; modalStatusColor = color }
    fun setOtpStatus(text: String, color: Color) { otpStatus = text; otpStatusColor = color }

    /** JS: openModal(mode, shopId) — ফিল্ড রিসেট + দেখানো */
    fun openModal(mode: ShopModalMode, shopId: String? = null) {
        shopName = ""; shopAddress = ""; shopDesc = ""; logoUrl = ""
        uploadText = "Tap to Upload Shop Logo"; uploadTextColor = TextLight
        setModalStatus("", Color.Black)
        deleteBtnEnabled = true; actionBtnEnabled = true
        modalMode = mode
        editShopId = if (mode == ShopModalMode.Edit) shopId.orEmpty() else ""
        showShopModal = true
    }

    fun closeShopModal() { showShopModal = false }
    fun closeOtpModal() { showOtpModal = false }
}

@Composable
fun ShopDashboardScreen(
    onShopSelected: (Shop) -> Unit,
    onLogout: () -> Unit
) {
    val shops by PosRepository.shops.collectAsState()
    val currentUser by PosRepository.currentUser.collectAsState()
    val state = remember { DashboardUiState() }

    LaunchedEffect(currentUser) {
        state.userName = currentUser?.name ?: "User"
    }

    LaunchedEffect(shops) {
        state.shops.clear()
        shops.forEach { s ->
            state.shops.add(ShopItem(s.id, s.name, s.type, s.image, "Owner", true))
        }
    }

    DashboardScreen(
        state = state,
        onEnterShop = { id, name ->
            val selected = shops.find { it.id == id } ?: Shop(id, name)
            PosRepository.selectShop(selected)
            onShopSelected(selected)
        },
        onCreateShop = {
            val newShop = PosRepository.addShop(state.shopName, state.shopDesc, state.shopAddress, "")
            state.closeShopModal()
            onShopSelected(newShop)
        },
        onUpdateShop = {
            state.closeShopModal()
        },
        onInitiateDelete = {
            state.otpEmail = currentUser?.email ?: "user@poysha.com"
            state.showOtpModal = true
        },
        onConfirmDelete = {
            state.closeOtpModal()
            state.closeShopModal()
        }
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState = remember { DashboardUiState() },
    onEnterShop: (id: String, name: String) -> Unit = { _, _ -> },
    onEditShopOpened: (id: String) -> Unit = {},      // fetchShopDataForEdit(id)
    onPickLogo: () -> Unit = {},                      // gallery খুলুন → compress → upload → state.logoUrl
    onCreateShop: () -> Unit = {},                    // saveShop()
    onUpdateShop: () -> Unit = {},                    // updateShop()
    onInitiateDelete: () -> Unit = {},                // initiateDelete()
    onConfirmDelete: () -> Unit = {},                 // confirmDeleteShop()
) {
    val modalOpen = state.showShopModal || state.showOtpModal

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 480.dp

        // body
        Box(
            Modifier.fillMaxSize()
                .then(if (modalOpen) Modifier.blur(6.dp, BlurredEdgeTreatment.Rectangle) else Modifier)
                .background(cssGradient(135f, 0f to Color(0xFF667EEA), 1f to Color(0xFF12604F))),
            contentAlignment = if (wide) Alignment.Center else Alignment.TopCenter
        ) {
            val radius = if (wide) 35f else 0f
            // .app-container
            Column(
                (if (wide) Modifier.width(450.dp).fillMaxHeight(0.9f) else Modifier.fillMaxSize().widthIn(max = 450.dp))
                    .cssShadow(Color.Black.copy(alpha = 0.15f), 0f, 20f, 40f, radius)
                    .clip(RoundedCornerShape(radius.dp))
                    .background(Color.White.copy(alpha = 0.95f))
            ) {
                // .top-bar
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.7f))
                        .drawBehind {
                            val h = 1.dp.toPx()
                            drawRect(Color(0xFFF3F4F6), Offset(0f, size.height - h), Size(size.width, h))
                        }
                        .padding(start = 25.dp, top = 35.dp, end = 25.dp, bottom = 20.dp + 1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RgbText("Hi, ${state.userName}")
                    Spacer(Modifier.height(5.dp))
                    Text("Welcome! Select a shop to manage.", Modifier.fillMaxWidth(), ts(13f, FontWeight.Medium, TextLight, align = TextAlign.Center))
                }

                // .container
                Column(
                    Modifier.weight(1f).fillMaxWidth().background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, top = 25.dp, end = 20.dp, bottom = 40.dp)
                ) {
                    AddShopCard { state.openModal(ShopModalMode.Create) }

                    if (state.loading) LoaderBlock()

                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        if (!state.loading) {
                            if (state.loadFailed) {
                                EmptyState("\uf071", "Connection Error", "Failed to load data. Check internet.")
                            } else if (state.shops.isEmpty()) {
                                EmptyState("\uf552", "No Shops Found", "You don't have any shops connected to this number. Create one to get started!")
                            } else {
                                state.shops.forEach { shop ->
                                    ShopCard(
                                        shop,
                                        onSettings = { state.openModal(ShopModalMode.Edit, shop.id); onEditShopOpened(shop.id) },
                                        onEnter = { onEnterShop(shop.id, shop.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- #shopModal ----
        if (state.showShopModal) {
            ModalOverlay(onClose = { state.closeShopModal() }) {
                val editing = state.modalMode == ShopModalMode.Edit
                Text(
                    if (editing) "Edit Shop Settings" else "Create New Shop",
                    Modifier.fillMaxWidth().padding(bottom = 25.dp),
                    ts(22f, FontWeight.ExtraBold, TextDark)
                )

                // .image-upload-wrapper
                Column(
                    Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .dashedBorder(Color(0xFFCBD5E1), 2f, 16f)
                        .clickable(remember { MutableInteractionSource() }, indication = null) { onPickLogo() }
                        .padding(17.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.logoUrl.isEmpty()) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Box(Modifier.height(38.dp)) { Fa("\uf0ee", 24f, Primary) }
                            Text(state.uploadText, style = ts(12f, FontWeight.SemiBold, state.uploadTextColor))
                        }
                    } else {
                        AsyncImage(
                            model = state.logoUrl, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp)
                                .cssShadow(Color.Black.copy(alpha = 0.1f), 0f, 4f, 10f, 12f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                FormField("Shop Name", true, state.shopName, { state.shopName = it }, "e.g. Allahardan Stationery")
                FormField("Tagline / Description", false, state.shopDesc, { state.shopDesc = it }, "e.g. Best wholesale shop in town", multiline = true)
                FormField("Physical Address", true, state.shopAddress, { state.shopAddress = it }, "Enter full address", multiline = true)

                GradientButton(
                    text = if (editing) "Update Shop Details" else "Create Shop",
                    enabled = state.actionBtnEnabled,
                    angle = 135f, c0 = Primary, c1 = Secondary,
                    shadowColor = Primary.copy(alpha = 0.24f), shadowBlur = 12f, shadowDy = 4f,
                    fontSize = 15f, weight = FontWeight.Bold, radius = 14f,
                ) {
                    val name = state.shopName.trim(); val addr = state.shopAddress.trim()
                    if (name.isEmpty() || addr.isEmpty()) {
                        state.setModalStatus(
                            if (editing) "Name & Address cannot be empty!" else "Please provide Shop Name and Address!", Danger
                        )
                    } else if (editing) onUpdateShop() else onCreateShop()
                }

                if (editing) {
                    // .btn-danger
                    val shape = RoundedCornerShape(14.dp)
                    Row(
                        Modifier.padding(top = 15.dp).fillMaxWidth()
                            .pressable(1f, 300, state.deleteBtnEnabled) { onInitiateDelete() }
                            .clip(shape).border(2.dp, Danger, shape).padding(14.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Fa("\uf2ed", 14f, Danger)
                        Spacer(Modifier.width(12.dp)) // margin-right 8 + space
                        Text("Delete Shop Permanently", style = ts(14f, FontWeight.SemiBold, Danger))
                    }
                }

                StatusMsg(state.modalStatus, state.modalStatusColor)
            }
        }

        // ---- #otpModal ----
        if (state.showOtpModal) {
            ModalOverlay(onClose = { state.closeOtpModal() }) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.padding(bottom = 20.dp).size(70.dp).clip(CircleShape).background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) { Fa("\uf071", 30f, Danger) }

                    Text("Security Alert", Modifier.fillMaxWidth().padding(bottom = 10.dp), ts(22f, FontWeight.ExtraBold, TextDark, align = TextAlign.Center))

                    BasicText(
                        buildAnnotatedString {
                            append("To delete this shop, please verify your identity. A 4-digit code has been sent to:\n")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)) { append(state.otpEmail) }
                        },
                        Modifier.fillMaxWidth().padding(bottom = 25.dp),
                        style = ts(13f, FontWeight.Normal, TextLight, align = TextAlign.Center, lh = 1.5f)
                    )

                    FormField(
                        label = null, required = false, value = state.otpInput,
                        onChange = { state.otpInput = it.filter { c -> c.isDigit() } },
                        placeholder = "Enter 4-Digit OTP", number = true,
                        style = ts(20f, FontWeight.ExtraBold, TextDark, ls = 8f, align = TextAlign.Center),
                        vPad = 14f
                    )

                    GradientButton(
                        text = "Confirm Deletion", enabled = state.confirmDeleteEnabled,
                        angle = 135f, c0 = Danger, c1 = Color(0xFFDC2626),
                        shadowColor = Danger.copy(alpha = 0.3f), shadowBlur = 15f, shadowDy = 4f,
                        fontSize = 15f, weight = FontWeight.Bold, radius = 14f,
                    ) { onConfirmDelete() }

                    StatusMsg(state.otpStatus, state.otpStatusColor)
                }
            }
        }
    }
}

// ====================== components ======================

@Composable
private fun RgbText(text: String) {
    val t = rememberInfiniteTransition()
    val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)))
    val brush = remember(f) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val w = size.width
                val x0 = -6f * w * f
                return LinearGradientShader(
                    Offset(x0, 0f), Offset(x0 + 3f * w, 0f),
                    listOf(Primary, Secondary, Warning, Primary),
                    listOf(0f, 1f / 3f, 2f / 3f, 1f),
                    TileMode.Repeated
                )
            }
        }
    }
    BasicText(
        text, Modifier.fillMaxWidth(),
        style = ts(24f, FontWeight.ExtraBold, Color.Unspecified, align = TextAlign.Center).copy(brush = brush)
    )
}

@Composable
private fun AddShopCard(onClick: () -> Unit) {
    Column(
        Modifier.padding(bottom = 25.dp).fillMaxWidth()
            .pressable(0.97f, 300, true, onClick)
            .clip(RoundedCornerShape(20.dp)).background(Color(0xFFF8FAFC))
            .dashedBorder(Color(0xFFCBD5E1), 2f, 20f)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.padding(bottom = 10.dp)) { Fa("\uf54e", 28f, Primary) }
        Text("Create New Shop", style = ts(15f, FontWeight.Bold, Primary))
        Spacer(Modifier.height(4.dp))
        Text("Start a new business space", style = ts(12f, FontWeight.Normal, TextLight))
    }
}

@Composable
private fun LoaderBlock() {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val t = rememberInfiniteTransition()
        val r by t.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)))
        androidx.compose.foundation.Canvas(Modifier.padding(bottom = 15.dp).size(40.dp).rotate(r)) {
            val w = 4.dp.toPx()
            val d = Size(size.width - w, size.height - w)
            drawArc(Color(0xFFE0E7FF), 0f, 360f, false, Offset(w / 2, w / 2), d, style = Stroke(w))
            drawArc(Primary, -135f, 90f, false, Offset(w / 2, w / 2), d, style = Stroke(w))
        }
        Text("Loading your shops...", style = ts(13f, FontWeight.Medium, TextLight))
    }
}

@Composable
private fun EmptyState(glyph: String, title: String, sub: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.padding(bottom = 15.dp)) { Fa(glyph, 50f, Color(0xFFCBD5E1)) }
        Text(title, Modifier.padding(bottom = 5.dp), ts(16f, FontWeight.Bold, TextDark, align = TextAlign.Center))
        Text(sub, style = ts(13f, FontWeight.Normal, TextLight, align = TextAlign.Center))
    }
}

@Composable
private fun ShopCard(shop: ShopItem, onSettings: () -> Unit, onEnter: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val avatar = "https://ui-avatars.com/api/?name=${Uri.encode(shop.name)}&background=4F46E5&color=fff&size=150"
    Row(
        Modifier.fillMaxWidth()
            .cssShadow(Color.Black.copy(alpha = 0.05f), 0f, 4f, 6f, 20f, spread = -1f)
            .clip(shape).background(Color.White).border(1.dp, Color(0xFFF1F5F9), shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        val imgShape = RoundedCornerShape(14.dp)
        AsyncImage(
            model = shop.image?.takeIf { it.isNotBlank() } ?: avatar,
            contentDescription = "Shop Logo", contentScale = ContentScale.Crop,
            modifier = Modifier.size(65.dp).clip(imgShape).background(Color(0xFFF3F4F6)).border(1.dp, Color(0xFFE5E7EB), imgShape)
        )
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                val role = shop.role.replaceFirstChar { it.uppercase() }.uppercase()
                Text(
                    role,
                    Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E7FF)).padding(horizontal = 10.dp, vertical = 3.dp),
                    ts(10f, FontWeight.Bold, Primary, ls = 0.5f)
                )
                if (shop.isCreator) {
                    Box(
                        Modifier.clickable(remember { MutableInteractionSource() }, indication = null, onClick = onSettings).padding(5.dp)
                    ) { Fa("\uf013", 18f, Color(0xFFCBD5E1)) }
                }
            }
            BasicText(
                shop.name, Modifier.widthIn(max = 180.dp),
                style = ts(16f, FontWeight.Bold, TextDark), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            BasicText(
                shop.description?.takeIf { it.isNotEmpty() } ?: "No description provided.",
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                style = ts(12f, FontWeight.Normal, TextLight), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            val bshape = RoundedCornerShape(12.dp)
            Row(
                Modifier.fillMaxWidth()
                    .pressable(0.97f, 200, true, onEnter)
                    .cssShadow(Secondary.copy(alpha = 0.2f), 0f, 4f, 10f, 12f)
                    .clip(bshape).background(cssGradient(135f, 0f to Primary, 1f to Secondary))
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enter Dashboard", style = ts(13f, FontWeight.SemiBold, Color.White))
                Fa("\uf2f6", 13f, Color.White)
            }
        }
    }
}

@Composable
private fun ModalOverlay(onClose: () -> Unit, content: @Composable () -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))) }
    val dy = with(LocalDensity.current) { 20.dp.toPx() }
    Box(
        Modifier.fillMaxSize().background(Color(0xFF111827).copy(alpha = 0.7f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxWidth(0.9f).widthIn(max = 380.dp)
                .graphicsLayer { alpha = p.value; scaleX = 0.9f + 0.1f * p.value; scaleY = 0.9f + 0.1f * p.value; translationY = (1f - p.value) * dy }
                .cssShadow(Color.Black.copy(alpha = 0.25f), 0f, 25f, 50f, 28f, spread = -12f)
                .clip(RoundedCornerShape(28.dp)).background(Color.White)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 25.dp, vertical = 30.dp)) { content() }
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 25.dp).size(30.dp)
                    .clip(CircleShape).background(Color(0xFFF3F4F6))
                    .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClose),
                contentAlignment = Alignment.Center
            ) { Fa("\uf00d", 24f, Color(0xFF9CA3AF)) }
        }
    }
}

@Composable
private fun FormField(
    label: String?, required: Boolean, value: String, onChange: (String) -> Unit, placeholder: String,
    multiline: Boolean = false, number: Boolean = false,
    style: TextStyle = ts(14f, FontWeight.Normal, TextDark), vPad: Float = 14f,
) {
    val src = remember { MutableInteractionSource() }
    val focused by src.collectIsFocusedAsState()
    val border by animateColorAsState(if (focused) Primary else Color(0xFFE5E7EB), tween(300), label = "b")
    val bg by animateColorAsState(if (focused) Color.White else Color(0xFFF9FAFB), tween(300), label = "bg")
    val ring by animateFloatAsState(if (focused) 1f else 0f, tween(300), label = "ring")
    val shape = RoundedCornerShape(14.dp)

    Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        if (label != null) {
            BasicText(
                buildAnnotatedString {
                    append(label)
                    if (required) { append(" "); withStyle(SpanStyle(color = Color.Red)) { append("*") } }
                },
                Modifier.padding(bottom = 8.dp), style = ts(12f, FontWeight.SemiBold, TextDark)
            )
        }
        Box(
            Modifier.fillMaxWidth()
                .drawBehind {
                    if (ring > 0f) {
                        val s = 4.dp.toPx()
                        drawRoundRect(
                            Primary.copy(alpha = 0.1f * ring), Offset(-s, -s),
                            Size(size.width + 2 * s, size.height + 2 * s), CornerRadius(18.dp.toPx())
                        )
                    }
                }
                .clip(shape).background(bg).border(2.dp, border, shape)
        ) {
            BasicTextField(
                value = value, onValueChange = onChange, interactionSource = src, textStyle = style,
                singleLine = !multiline, minLines = if (multiline) 2 else 1, maxLines = if (multiline) 2 else 1,
                cursorBrush = SolidColor(TextDark),
                keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = (vPad + 2f).dp),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) BasicText(placeholder, Modifier.fillMaxWidth(), style = style.copy(color = Color(0xFF757575)))
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun GradientButton(
    text: String, enabled: Boolean, angle: Float, c0: Color, c1: Color,
    shadowColor: Color, shadowBlur: Float, shadowDy: Float,
    fontSize: Float, weight: FontWeight, radius: Float, onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(radius.dp)
    Box(
        Modifier.padding(top = 10.dp).fillMaxWidth()
            .graphicsLayer { alpha = if (enabled) 1f else 0.7f }
            .pressable(0.98f, 200, enabled, onClick)
            .cssShadow(shadowColor, 0f, shadowDy, shadowBlur, radius)
            .clip(shape).background(cssGradient(angle, 0f to c0, 1f to c1))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = ts(fontSize, weight, Color.White)) }
}

@Composable
private fun StatusMsg(text: String, color: Color) {
    Box(Modifier.padding(top = 15.dp).fillMaxWidth().heightIn(min = 20.dp), contentAlignment = Alignment.Center) {
        Text(text, Modifier.fillMaxWidth(), ts(13f, FontWeight.SemiBold, color, align = TextAlign.Center))
    }
}

// ====================== helpers (CSS → Compose) ======================

@Composable
private fun Text(t: String, modifier: Modifier = Modifier, style: TextStyle) = BasicText(t, modifier, style = style)

private fun ts(
    size: Float, weight: FontWeight = FontWeight.Normal, color: Color = TextDark,
    ls: Float = 0f, align: TextAlign = TextAlign.Unspecified, lh: Float = 1.5f,
) = TextStyle(
    fontFamily = Poppins, fontSize = size.sp, fontWeight = weight, color = color,
    letterSpacing = ls.sp, textAlign = align, lineHeight = (size * lh).sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

@Composable
private fun Fa(glyph: String, size: Float, color: Color, modifier: Modifier = Modifier) {
    BasicText(
        glyph, modifier,
        style = TextStyle(
            fontFamily = FaSolid, fontSize = size.sp, color = color, lineHeight = size.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
        )
    )
}

private fun cssGradient(angleDeg: Float, vararg stops: Pair<Float, Color>): Brush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val a = Math.toRadians(angleDeg.toDouble())
        val dx = kotlin.math.sin(a).toFloat()
        val dy = -kotlin.math.cos(a).toFloat()
        val len = kotlin.math.abs(size.width * dx) + kotlin.math.abs(size.height * dy)
        val cx = size.width / 2f
        val cy = size.height / 2f
        return LinearGradientShader(
            Offset(cx - dx * len / 2f, cy - dy * len / 2f),
            Offset(cx + dx * len / 2f, cy + dy * len / 2f),
            stops.map { it.second }, stops.map { it.first }
        )
    }
}

private fun Modifier.cssShadow(
    color: Color, dx: Float, dy: Float, blur: Float, radius: Float, spread: Float = 0f,
): Modifier = drawBehind {
    if (color.alpha == 0f) return@drawBehind
    drawIntoCanvas { canvas ->
        val p = Paint()
        val fp = p.asFrameworkPaint()
        fp.isAntiAlias = true
        fp.color = android.graphics.Color.WHITE
        fp.setShadowLayer(blur.dp.toPx(), dx.dp.toPx(), dy.dp.toPx(), color.toArgb())
        val sp = spread.dp.toPx()
        val r = (radius + spread).coerceAtLeast(0f).dp.toPx()
        canvas.drawRoundRect(-sp, -sp, size.width + sp, size.height + sp, r, r, p)
    }
}

private fun Modifier.dashedBorder(color: Color, width: Float, radius: Float): Modifier = drawBehind {
    val w = width.dp.toPx()
    drawRoundRect(
        color, Offset(w / 2, w / 2), Size(size.width - w, size.height - w),
        CornerRadius(radius.dp.toPx() - w / 2),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * w, 2f * w)))
    )
}

private fun Modifier.pressable(scale: Float, ms: Int = 200, enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed && enabled) scale else 1f, tween(ms), label = "press")
    this.graphicsLayer { scaleX = s; scaleY = s }
        .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick)
}
