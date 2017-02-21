/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2016 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.libadblockplus.android.webview;

import android.content.Context;
import android.util.AttributeSet;

import org.adblockplus.libadblockplus.android.AdblockEngine;

/**
 * WebView with ad blocking
 */
public class AdblockWebView extends AdblockWebView4 {

  public AdblockWebView(Context context) {
    super(context);
  }

  public AdblockWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AdblockWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public AdblockWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setAdblockEngine(final AdblockEngine adblockEngine) {
    super.setAdblockEngine(adblockEngine);
  }

}
