/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material

import android.os.SystemClock.sleep
import androidx.compose.foundation.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Providers
import androidx.compose.runtime.emptyContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.onPositioned
import androidx.compose.ui.platform.LayoutDirectionAmbient
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.filters.MediumTest
import androidx.ui.test.GestureScope
import androidx.ui.test.assertIsEqualTo
import androidx.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.ui.test.assertTopPositionInRootIsEqualTo
import androidx.ui.test.assertWidthIsEqualTo
import androidx.ui.test.bottomCenter
import androidx.ui.test.center
import androidx.ui.test.centerLeft
import androidx.ui.test.click
import androidx.ui.test.createComposeRule
import androidx.ui.test.onNodeWithTag
import androidx.ui.test.performGesture
import androidx.ui.test.runOnIdle
import androidx.ui.test.runOnUiThread
import androidx.ui.test.swipe
import androidx.ui.test.swipeDown
import androidx.ui.test.swipeLeft
import androidx.ui.test.swipeRight
import androidx.ui.test.swipeUp
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@MediumTest
@RunWith(JUnit4::class)
class DrawerTest {

    @get:Rule
    val composeTestRule = createComposeRule(disableTransitions = true)

    @Test
    fun modalDrawer_testOffset_whenOpen() {
        composeTestRule.setMaterialContent {
            val drawerState = rememberDrawerState(DrawerValue.Open)
            ModalDrawerLayout(drawerState, drawerContent = {
                Box(Modifier.fillMaxSize().testTag("content"))
            }, bodyContent = emptyContent())
        }

        onNodeWithTag("content")
            .assertLeftPositionInRootIsEqualTo(0.dp)
    }

    @Test
    fun modalDrawer_testOffset_whenClosed() {
        var position: Offset? = null
        composeTestRule.setMaterialContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            ModalDrawerLayout(drawerState, drawerContent = {
                Box(Modifier.fillMaxSize().onPositioned { coords: LayoutCoordinates ->
                    position = coords.localToRoot(Offset.Zero)
                })
            }, bodyContent = emptyContent())
        }

        val width = rootWidth()
        composeTestRule.runOnIdleWithDensity {
            position!!.x.toDp().assertIsEqualTo(-width)
        }
    }

    @Test
    fun modalDrawer_testEndPadding_whenOpen() {
        composeTestRule.setMaterialContent {
            val drawerState = rememberDrawerState(DrawerValue.Open)
            ModalDrawerLayout(drawerState, drawerContent = {
                Box(Modifier.fillMaxSize().testTag("content"))
            }, bodyContent = emptyContent())
        }

        onNodeWithTag("content")
            .assertWidthIsEqualTo(rootWidth() - 56.dp)
    }

    @Test
    fun bottomDrawer_testOffset_whenOpen() {
        composeTestRule.setMaterialContent {
            val drawerState = rememberBottomDrawerState(BottomDrawerValue.Open)
            BottomDrawerLayout(drawerState, drawerContent = {
                Box(Modifier.fillMaxSize().testTag("content"))
            }, bodyContent = emptyContent())
        }

        val width = rootWidth()
        val height = rootHeight()
        val expectedHeight = if (width > height) 0.dp else (height / 2)
        onNodeWithTag("content")
            .assertTopPositionInRootIsEqualTo(expectedHeight)
    }

    @Test
    fun bottomDrawer_testOffset_whenClosed() {
        var position: Offset? = null
        composeTestRule.setMaterialContent {
            val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
            BottomDrawerLayout(drawerState, drawerContent = {
                Box(Modifier.fillMaxSize().onPositioned { coords: LayoutCoordinates ->
                    position = coords.localToRoot(Offset.Zero)
                })
            }, bodyContent = emptyContent())
        }

        val height = rootHeight()
        composeTestRule.runOnIdleWithDensity {
            position!!.y.toDp().assertIsEqualTo(height)
        }
    }

    @Test
    @Ignore("failing in postsubmit, fix in b/148751721")
    fun modalDrawer_openAndClose() {
        var contentWidth: Int? = null
        var openedLatch: CountDownLatch? = null
        var closedLatch: CountDownLatch? = CountDownLatch(1)
        lateinit var drawerState: DrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberDrawerState(DrawerValue.Closed)
            ModalDrawerLayout(drawerState,
                drawerContent = {
                    Box(
                        Modifier.fillMaxSize().onPositioned { info: LayoutCoordinates ->
                            val pos = info.localToGlobal(Offset.Zero)
                            if (pos.x == 0.0f) {
                                // If fully opened, mark the openedLatch if present
                                openedLatch?.countDown()
                            } else if (-pos.x.roundToInt() == contentWidth) {
                                // If fully closed, mark the closedLatch if present
                                closedLatch?.countDown()
                            }
                        }
                    )
                },
                bodyContent = {
                    Box(Modifier.fillMaxSize()
                        .onPositioned { contentWidth = it.size.width })
                })
        }
        // Drawer should start in closed state
        assertThat(closedLatch!!.await(5, TimeUnit.SECONDS)).isTrue()

        // When the drawer state is set to Open
        openedLatch = CountDownLatch(1)
        runOnIdle {
            drawerState.open()
        }
        // Then the drawer should be opened
        assertThat(openedLatch.await(5, TimeUnit.SECONDS)).isTrue()

        // When the drawer state is set to Closed
        closedLatch = CountDownLatch(1)
        runOnIdle {
            drawerState.close()
        }
        // Then the drawer should be closed
        assertThat(closedLatch.await(5, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun modalDrawer_bodyContent_clickable() {
        var drawerClicks = 0
        var bodyClicks = 0
        lateinit var drawerState: DrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberDrawerState(DrawerValue.Closed)
            // emulate click on the screen
            ModalDrawerLayout(drawerState,
                drawerContent = {
                    Box(
                        Modifier.fillMaxSize().clickable { drawerClicks += 1 },
                        children = emptyContent()
                    )
                },
                bodyContent = {
                    Box(
                        Modifier.testTag("Drawer").fillMaxSize().clickable { bodyClicks += 1 },
                        children = emptyContent()
                    )
                })
        }

        // Click in the middle of the drawer (which is the middle of the body)
        onNodeWithTag("Drawer").performGesture { click() }

        runOnIdle {
            assertThat(drawerClicks).isEqualTo(0)
            assertThat(bodyClicks).isEqualTo(1)

            drawerState.open()
        }
        sleep(100) // TODO(147586311): remove this sleep when opening the drawer triggers a wait

        // Click on the left-center pixel of the drawer
        onNodeWithTag("Drawer").performGesture {
            click(centerLeft)
        }

        runOnIdle {
            assertThat(drawerClicks).isEqualTo(1)
            assertThat(bodyClicks).isEqualTo(1)
        }
    }

    @Test
    @Ignore("failing in postsubmit, fix in b/148751721")
    fun bottomDrawer_openAndClose() {
        var contentHeight: Int? = null
        var openedHeight: Int? = null
        var openedLatch: CountDownLatch? = null
        var closedLatch: CountDownLatch? = CountDownLatch(1)
        lateinit var drawerState: BottomDrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
            BottomDrawerLayout(drawerState,
                drawerContent = {
                    Box(Modifier.fillMaxSize().onPositioned { info: LayoutCoordinates ->
                        val pos = info.localToGlobal(Offset.Zero)
                        if (pos.y.roundToInt() == openedHeight) {
                            // If fully opened, mark the openedLatch if present
                            openedLatch?.countDown()
                        } else if (pos.y.roundToInt() == contentHeight) {
                            // If fully closed, mark the closedLatch if present
                            closedLatch?.countDown()
                        }
                    })
                },
                bodyContent = {
                    Box(Modifier.fillMaxSize().onPositioned {
                        contentHeight = it.size.height
                        openedHeight = (it.size.height * BottomDrawerOpenFraction).roundToInt()
                    })
                }
            )
        }
        // Drawer should start in closed state
        assertThat(closedLatch!!.await(5, TimeUnit.SECONDS)).isTrue()

        // When the drawer state is set to Open
        openedLatch = CountDownLatch(1)
        runOnIdle {
            drawerState.open()
        }
        // Then the drawer should be opened
        assertThat(openedLatch.await(5, TimeUnit.SECONDS)).isTrue()

        // When the drawer state is set to Closed
        closedLatch = CountDownLatch(1)
        runOnIdle {
            drawerState.close()
        }
        // Then the drawer should be closed
        assertThat(closedLatch.await(5, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun bottomDrawer_bodyContent_clickable() {
        var drawerClicks = 0
        var bodyClicks = 0
        lateinit var drawerState: BottomDrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
            // emulate click on the screen
            BottomDrawerLayout(drawerState,
                drawerContent = {
                    Box(
                        Modifier.fillMaxSize().clickable { drawerClicks += 1 },
                        children = emptyContent()
                    )
                },
                bodyContent = {
                    Box(
                        Modifier.testTag("Drawer").fillMaxSize().clickable { bodyClicks += 1 },
                        children = emptyContent()
                    )
                })
        }

        // Click in the middle of the drawer (which is the middle of the body)
        onNodeWithTag("Drawer").performGesture { click() }

        runOnIdle {
            assertThat(drawerClicks).isEqualTo(0)
            assertThat(bodyClicks).isEqualTo(1)
        }

        runOnUiThread {
            drawerState.open()
        }
        sleep(100) // TODO(147586311): remove this sleep when opening the drawer triggers a wait

        // Click on the bottom-center pixel of the drawer
        onNodeWithTag("Drawer").performGesture {
            click(bottomCenter)
        }

        assertThat(drawerClicks).isEqualTo(1)
        assertThat(bodyClicks).isEqualTo(1)
    }

    @Test
    fun modalDrawer_openBySwipe() {
        lateinit var drawerState: DrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberDrawerState(DrawerValue.Closed)
            // emulate click on the screen
            Box(Modifier.testTag("Drawer")) {
                ModalDrawerLayout(drawerState,
                    drawerContent = {
                        Box(Modifier.fillMaxSize().background(color = Color.Magenta))
                    },
                    bodyContent = {
                        Box(Modifier.fillMaxSize().background(color = Color.Red))
                    })
            }
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeRight() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(DrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeLeft() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(DrawerValue.Closed)
        }
    }

    @Test
    fun modalDrawer_openBySwipe_rtl() {
        lateinit var drawerState: DrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberDrawerState(DrawerValue.Closed)
            // emulate click on the screen
            Providers(LayoutDirectionAmbient provides LayoutDirection.Rtl) {
                Box(Modifier.testTag("Drawer")) {
                    ModalDrawerLayout(drawerState,
                        drawerContent = {
                            Box(Modifier.fillMaxSize().background(color = Color.Magenta))
                        },
                        bodyContent = {
                            Box(Modifier.fillMaxSize().background(color = Color.Red))
                        })
                }
            }
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeLeft() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(DrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeRight() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(DrawerValue.Closed)
        }
    }

    @Test
    fun bottomDrawer_openBySwipe() {
        lateinit var drawerState: BottomDrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
            // emulate click on the screen
            Box(Modifier.testTag("Drawer")) {
                BottomDrawerLayout(drawerState,
                    drawerContent = {
                        Box(Modifier.fillMaxSize().background(color = Color.Magenta))
                    },
                    bodyContent = {
                        Box(Modifier.fillMaxSize().background(color = Color.Red))
                    })
            }
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeUp() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Expanded)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeDown() }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Closed)
        }
    }

    @Test
    fun bottomDrawer_openBySwipe_thresholds() {
        lateinit var drawerState: BottomDrawerState
        composeTestRule.setMaterialContent {
            drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
            // emulate click on the screen
            Box(Modifier.testTag("Drawer")) {
                BottomDrawerLayout(drawerState,
                    drawerContent = {
                        Box(Modifier.fillMaxSize().background(Color.Magenta))
                    },
                    bodyContent = {
                        Box(Modifier.fillMaxSize().background(Color.Red))
                    })
            }
        }
        val threshold = with (composeTestRule.density) { BottomDrawerThreshold.toPx() }

        onNodeWithTag("Drawer")
            .performGesture { swipeUpBy(threshold / 2) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Closed)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeUpBy(threshold) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeUpBy(threshold / 2) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeUpBy(threshold) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Expanded)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeDownBy(threshold / 2) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Expanded)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeDownBy(threshold) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeDownBy(threshold / 2) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Open)
        }

        onNodeWithTag("Drawer")
            .performGesture { swipeDownBy(threshold) }

        runOnIdle {
            assertThat(drawerState.value).isEqualTo(BottomDrawerValue.Closed)
        }
    }

    private fun GestureScope.swipeUpBy(offset: Float) {
        swipe(center, center.copy(y = center.y - offset))
    }

    private fun GestureScope.swipeDownBy(offset: Float) {
        swipe(center, center.copy(y = center.y + offset))
    }
}
