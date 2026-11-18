package com.abrarshakhi.mishti.models.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.abrarshakhi.mishti.core.designsystem.resources.action_back
import com.abrarshakhi.mishti.core.designsystem.resources.action_cancel
import com.abrarshakhi.mishti.core.designsystem.resources.action_delete
import com.abrarshakhi.mishti.core.designsystem.resources.models_title
import com.abrarshakhi.mishti.core.designsystem.theme.MishtiTheme
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.device.DeviceCapability
import com.abrarshakhi.mishti.feature.model.resources.models_delete_downloaded_body
import com.abrarshakhi.mishti.feature.model.resources.models_delete_imported_body
import com.abrarshakhi.mishti.feature.model.resources.models_delete_title
import com.abrarshakhi.mishti.feature.model.resources.models_import
import com.abrarshakhi.mishti.feature.model.resources.models_storage
import com.abrarshakhi.mishti.feature.model.resources.models_tab_browse
import com.abrarshakhi.mishti.feature.model.resources.models_tab_shelf
import com.abrarshakhi.mishti.models.domain.model.Catalog
import com.abrarshakhi.mishti.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.CatalogState
import com.abrarshakhi.mishti.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.models.domain.model.memoryFit
import com.abrarshakhi.mishti.models.domain.repository.StorageUsage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.core.designsystem.resources.Res as CoreDesignSystemRes
import com.abrarshakhi.mishti.feature.model.resources.Res as FeatureModelRes

internal const val ShelfPage = 0
internal const val BrowsePage = 1
