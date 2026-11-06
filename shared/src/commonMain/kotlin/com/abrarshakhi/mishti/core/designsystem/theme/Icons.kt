package com.abrarshakhi.mishti.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.phosphor.icons.PhIcons
import com.phosphor.icons.filled.ArchiveFill
import com.phosphor.icons.filled.ArrowCircleLeftFill
import com.phosphor.icons.filled.ArrowCircleRightFill
import com.phosphor.icons.filled.ArrowCounterClockwiseFill
import com.phosphor.icons.filled.ArrowSquareOutFill
import com.phosphor.icons.filled.BrainFill
import com.phosphor.icons.filled.BroomFill
import com.phosphor.icons.filled.BugDroidFill
import com.phosphor.icons.filled.ChatTeardropTextFill
import com.phosphor.icons.filled.ChatsTeardropFill
import com.phosphor.icons.filled.CheckFill
import com.phosphor.icons.filled.CircleHalfFill
import com.phosphor.icons.filled.CloudArrowDownFill
import com.phosphor.icons.filled.CloudSlashFill
import com.phosphor.icons.filled.CopySimpleFill
import com.phosphor.icons.filled.DotsThreeCircleVerticalFill
import com.phosphor.icons.filled.DotsThreeFill
import com.phosphor.icons.filled.FadersHorizontalFill
import com.phosphor.icons.filled.FileArrowDownFill
import com.phosphor.icons.filled.GaugeFill
import com.phosphor.icons.filled.GavelFill
import com.phosphor.icons.filled.GearSixFill
import com.phosphor.icons.filled.GithubLogoFill
import com.phosphor.icons.filled.GlobeHemisphereEastFill
import com.phosphor.icons.filled.GoogleChromeLogoFill
import com.phosphor.icons.filled.HandFistFill
import com.phosphor.icons.filled.HeartFill
import com.phosphor.icons.filled.LightbulbFill
import com.phosphor.icons.filled.MagnifyingGlassFill
import com.phosphor.icons.filled.MoonStarsFill
import com.phosphor.icons.filled.PaintRollerFill
import com.phosphor.icons.filled.PaperPlaneTiltFill
import com.phosphor.icons.filled.PencilFill
import com.phosphor.icons.filled.SealWarningFill
import com.phosphor.icons.filled.ShieldCheckFill
import com.phosphor.icons.filled.ShuffleFill
import com.phosphor.icons.filled.SidebarSimpleFill
import com.phosphor.icons.filled.StarFill
import com.phosphor.icons.filled.StopCircleFill
import com.phosphor.icons.filled.StorefrontFill
import com.phosphor.icons.filled.TrashFill
import com.phosphor.icons.filled.XFill
import com.phosphor.icons.regular.Archive
import com.phosphor.icons.regular.ArrowCircleLeft
import com.phosphor.icons.regular.ArrowCircleRight
import com.phosphor.icons.regular.ArrowCounterClockwise
import com.phosphor.icons.regular.ArrowSquareOut
import com.phosphor.icons.regular.Brain
import com.phosphor.icons.regular.Broom
import com.phosphor.icons.regular.BugDroid
import com.phosphor.icons.regular.ChatTeardropText
import com.phosphor.icons.regular.ChatsTeardrop
import com.phosphor.icons.regular.Check
import com.phosphor.icons.regular.CircleHalf
import com.phosphor.icons.regular.CloudArrowDown
import com.phosphor.icons.regular.CloudSlash
import com.phosphor.icons.regular.CopySimple
import com.phosphor.icons.regular.DotsThree
import com.phosphor.icons.regular.DotsThreeCircleVertical
import com.phosphor.icons.regular.FadersHorizontal
import com.phosphor.icons.regular.FileArrowDown
import com.phosphor.icons.regular.Gauge
import com.phosphor.icons.regular.Gavel
import com.phosphor.icons.regular.GearSix
import com.phosphor.icons.regular.GithubLogo
import com.phosphor.icons.regular.GlobeHemisphereEast
import com.phosphor.icons.regular.GoogleChromeLogo
import com.phosphor.icons.regular.HandFist
import com.phosphor.icons.regular.Heart
import com.phosphor.icons.regular.Lightbulb
import com.phosphor.icons.regular.MagnifyingGlass
import com.phosphor.icons.regular.MoonStars
import com.phosphor.icons.regular.PaintRoller
import com.phosphor.icons.regular.PaperPlaneTilt
import com.phosphor.icons.regular.Pencil
import com.phosphor.icons.regular.SealWarning
import com.phosphor.icons.regular.ShieldCheck
import com.phosphor.icons.regular.Shuffle
import com.phosphor.icons.regular.SidebarSimple
import com.phosphor.icons.regular.Star
import com.phosphor.icons.regular.StopCircle
import com.phosphor.icons.regular.Storefront
import com.phosphor.icons.regular.Trash
import com.phosphor.icons.regular.X

@Immutable
data class MishtiIcon(val filled: ImageVector, val outline: ImageVector)

@Immutable
data class ActionIcons(
    val newChat: MishtiIcon,
    val moreRounded: MishtiIcon,
    val edit: MishtiIcon,
    val delete: MishtiIcon,
    val send: MishtiIcon,
    val pause: MishtiIcon,
    val thinking: MishtiIcon,
    val shuffle: MishtiIcon,
    val copy: MishtiIcon,
    val check: MishtiIcon,
    val more: MishtiIcon,
    val search: MishtiIcon,
    val clear: MishtiIcon,
    val download: MishtiIcon,
    val cancel: MishtiIcon,
    val redo: MishtiIcon,
    val importFile: MishtiIcon,
    val autoBrightness: MishtiIcon,
    val lightTheme: MishtiIcon,
    val darkTheme: MishtiIcon,
)

@Immutable
data class NavigationIcons(
    val back: MishtiIcon,
    val foreword: MishtiIcon,
    val menu: MishtiIcon,
    val settings: MishtiIcon,
    val store: MishtiIcon,
    val global: MishtiIcon,
    val browser: MishtiIcon,
    val shelf: MishtiIcon,
    val theme: MishtiIcon,
    val chat: MishtiIcon,
    val config: MishtiIcon,
    val speed: MishtiIcon,
    val favorite: MishtiIcon,
    val privacy: MishtiIcon,
    val gavel: MishtiIcon,
    val github: MishtiIcon,
    val bug: MishtiIcon,
    val volunteerActivism: MishtiIcon,
    val star: MishtiIcon,
    val arrowOut: MishtiIcon,
)

@Immutable
data class VisualIcons(val warning: MishtiIcon, val notOnline: MishtiIcon, val error: MishtiIcon)

@Immutable
data class MishtiIcons(val actions: ActionIcons, val navigation: NavigationIcons, val visuals: VisualIcons)

@Composable
fun defaultMishtiIcons(): MishtiIcons = MishtiIcons(
    actions = ActionIcons(
        newChat = MishtiIcon(PhIcons.Filled.ChatsTeardropFill, PhIcons.Regular.ChatsTeardrop),
        moreRounded = MishtiIcon(
            PhIcons.Filled.DotsThreeCircleVerticalFill,
            PhIcons.Regular.DotsThreeCircleVertical,
        ),
        edit = MishtiIcon(PhIcons.Filled.PencilFill, PhIcons.Regular.Pencil),
        delete = MishtiIcon(PhIcons.Filled.TrashFill, PhIcons.Regular.Trash),
        send = MishtiIcon(PhIcons.Filled.PaperPlaneTiltFill, PhIcons.Regular.PaperPlaneTilt),
        pause = MishtiIcon(PhIcons.Filled.StopCircleFill, PhIcons.Regular.StopCircle),
        thinking = MishtiIcon(PhIcons.Filled.BrainFill, PhIcons.Regular.Brain),
        shuffle = MishtiIcon(PhIcons.Filled.ShuffleFill, PhIcons.Regular.Shuffle),
        copy = MishtiIcon(PhIcons.Filled.CopySimpleFill, PhIcons.Regular.CopySimple),
        check = MishtiIcon(PhIcons.Filled.CheckFill, PhIcons.Regular.Check),
        more = MishtiIcon(PhIcons.Filled.DotsThreeFill, PhIcons.Regular.DotsThree),
        search = MishtiIcon(
            PhIcons.Filled.MagnifyingGlassFill,
            PhIcons.Regular.MagnifyingGlass,
        ),
        clear = MishtiIcon(PhIcons.Filled.BroomFill, PhIcons.Regular.Broom),
        download = MishtiIcon(
            PhIcons.Filled.CloudArrowDownFill,
            PhIcons.Regular.CloudArrowDown,
        ),
        cancel = MishtiIcon(PhIcons.Filled.XFill, PhIcons.Regular.X),
        redo = MishtiIcon(
            PhIcons.Filled.ArrowCounterClockwiseFill,
            PhIcons.Regular.ArrowCounterClockwise,
        ),
        importFile = MishtiIcon(
            PhIcons.Filled.FileArrowDownFill,
            PhIcons.Regular.FileArrowDown,
        ),
        autoBrightness = MishtiIcon(PhIcons.Filled.CircleHalfFill, PhIcons.Regular.CircleHalf),
        lightTheme = MishtiIcon(PhIcons.Filled.LightbulbFill, PhIcons.Regular.Lightbulb),
        darkTheme = MishtiIcon(PhIcons.Filled.MoonStarsFill, PhIcons.Regular.MoonStars),
    ),
    navigation = NavigationIcons(
        back = MishtiIcon(PhIcons.Filled.ArrowCircleLeftFill, PhIcons.Regular.ArrowCircleLeft),
        foreword = MishtiIcon(
            PhIcons.Filled.ArrowCircleRightFill,
            PhIcons.Regular.ArrowCircleRight,
        ),
        menu = MishtiIcon(PhIcons.Filled.SidebarSimpleFill, PhIcons.Regular.SidebarSimple),
        settings = MishtiIcon(PhIcons.Filled.GearSixFill, PhIcons.Regular.GearSix),
        store = MishtiIcon(PhIcons.Filled.StorefrontFill, PhIcons.Regular.Storefront),
        global = MishtiIcon(
            PhIcons.Filled.GlobeHemisphereEastFill,
            PhIcons.Regular.GlobeHemisphereEast,
        ),
        browser = MishtiIcon(
            PhIcons.Filled.GoogleChromeLogoFill,
            PhIcons.Regular.GoogleChromeLogo,
        ),
        shelf = MishtiIcon(PhIcons.Filled.ArchiveFill, PhIcons.Regular.Archive),
        theme = MishtiIcon(PhIcons.Filled.PaintRollerFill, PhIcons.Regular.PaintRoller),
        chat = MishtiIcon(PhIcons.Filled.ChatTeardropTextFill, PhIcons.Regular.ChatTeardropText),
        config = MishtiIcon(
            PhIcons.Filled.FadersHorizontalFill,
            PhIcons.Regular.FadersHorizontal,
        ),
        speed = MishtiIcon(PhIcons.Filled.GaugeFill, PhIcons.Regular.Gauge),
        favorite = MishtiIcon(PhIcons.Filled.HeartFill, PhIcons.Regular.Heart),
        privacy = MishtiIcon(PhIcons.Filled.ShieldCheckFill, PhIcons.Regular.ShieldCheck),
        gavel = MishtiIcon(PhIcons.Filled.GavelFill, PhIcons.Regular.Gavel),
        github = MishtiIcon(PhIcons.Filled.GithubLogoFill, PhIcons.Regular.GithubLogo),
        bug = MishtiIcon(PhIcons.Filled.BugDroidFill, PhIcons.Regular.BugDroid),
        volunteerActivism = MishtiIcon(PhIcons.Filled.HandFistFill, PhIcons.Regular.HandFist),
        star = MishtiIcon(PhIcons.Filled.StarFill, PhIcons.Regular.Star),
        arrowOut = MishtiIcon(PhIcons.Filled.ArrowSquareOutFill, PhIcons.Regular.ArrowSquareOut),
    ),
    visuals = VisualIcons(
        warning = MishtiIcon(PhIcons.Filled.SealWarningFill, PhIcons.Regular.SealWarning),
        notOnline = MishtiIcon(PhIcons.Filled.CloudSlashFill, PhIcons.Regular.CloudSlash),
        error = MishtiIcon(PhIcons.Filled.BugDroidFill, PhIcons.Regular.BugDroid),
    ),
)

val MaterialTheme.icons: MishtiIcons
    @Composable
    @ReadOnlyComposable
    get() = LocalMishtiIcons.current

val LocalMishtiIcons = compositionLocalOf<MishtiIcons> {
    error("No MishtiIcons provided. Ensure your content is wrapped in MishtiTheme.")
}
