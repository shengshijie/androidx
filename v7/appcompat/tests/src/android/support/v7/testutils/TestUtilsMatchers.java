/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.support.v7.testutils;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class TestUtilsMatchers {
    /**
     * Returns a matcher that matches <code>ImageView</code>s which have drawable flat-filled
     * with the specific color.
     */
    public static Matcher drawable(@ColorInt final int color) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            private String failedComparisonDescription;

            @Override
            public void describeTo(final Description description) {
                description.appendText("with drawable of color: ");

                description.appendText(failedComparisonDescription);
            }

            @Override
            public boolean matchesSafely(final ImageView view) {
                Drawable drawable = view.getDrawable();
                if (drawable == null) {
                    return false;
                }

                // One option is to check if we have a ColorDrawable and then call getColor
                // but that API is v11+. Instead, we call our helper method that checks whether
                // all pixels in a Drawable are of the same specified color.
                try {
                    TestUtils.assertAllPixelsOfColor("", drawable, view.getWidth(),
                            view.getHeight(), color, true);
                    // If we are here, the color comparison has passed.
                    failedComparisonDescription = null;
                    return true;
                } catch (Throwable t) {
                    // If we are here, the color comparison has failed.
                    failedComparisonDescription = t.getMessage();
                    return false;
                }
            }
        };
    }

    /**
     * Returns a matcher that matches <code>CheckedTextView</code>s which are in checked state.
     */
    public static Matcher isCheckedTextView() {
        return new BoundedMatcher<View, CheckedTextView>(CheckedTextView.class) {
            private String failedDescription;

            @Override
            public void describeTo(final Description description) {
                description.appendText("checked text view: ");

                description.appendText(failedDescription);
            }

            @Override
            public boolean matchesSafely(final CheckedTextView view) {
                if (view.isChecked()) {
                    return true;
                }

                failedDescription = "not checked";
                return false;
            }
        };
    }

    /**
     * Returns a matcher that matches <code>CheckedTextView</code>s which are in checked state.
     */
    public static Matcher isNonCheckedTextView() {
        return new BoundedMatcher<View, CheckedTextView>(CheckedTextView.class) {
            private String failedDescription;

            @Override
            public void describeTo(final Description description) {
                description.appendText("non checked text view: ");

                description.appendText(failedDescription);
            }

            @Override
            public boolean matchesSafely(final CheckedTextView view) {
                if (!view.isChecked()) {
                    return true;
                }

                failedDescription = "checked";
                return false;
            }
        };
    }
}
