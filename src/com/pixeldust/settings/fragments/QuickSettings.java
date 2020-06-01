/*
 * Copyright (C) 2017-2020 The PixelDust Project
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
package com.pixeldust.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.pixeldust.settings.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String QS_BRIGHTNESS_SLIDER_FOOTER = "qs_brightness_slider_footer";
    private static final String QS_SHOW_BRIGHTNESS = "qs_show_brightness";

    private SystemSettingSwitchPreference mFooterSlider;
    private SystemSettingSwitchPreference mQSSlider;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pixeldust_settings_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();

        boolean isFooterSlider = Settings.System.getIntForUser(getActivity().getContentResolver(),
                QS_BRIGHTNESS_SLIDER_FOOTER, 0, UserHandle.USER_CURRENT) != 0;
        mFooterSlider =
                (SystemSettingSwitchPreference) findPreference(QS_BRIGHTNESS_SLIDER_FOOTER);
        mFooterSlider.setChecked(isFooterSlider);
        mFooterSlider.setOnPreferenceChangeListener(this);

        mQSSlider =
                (SystemSettingSwitchPreference) findPreference(QS_SHOW_BRIGHTNESS);
        mQSSlider.setOnPreferenceChangeListener(this);

        updateDependencies(mFooterSlider, isFooterSlider);
    }

    private void updateDependencies(Preference updatedPreference, Boolean newValue) {
        if (updatedPreference == mFooterSlider) {
            mFooterSlider.setChecked(newValue);
            if (newValue) mQSSlider.setChecked(false);
        } else if (updatedPreference == mQSSlider) {
            mQSSlider.setChecked(newValue);
            if (newValue) mFooterSlider.setChecked(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterSlider) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    QS_BRIGHTNESS_SLIDER_FOOTER, value ? 1 : 0, UserHandle.USER_CURRENT);
            if (value) {
                Settings.System.putIntForUser(getActivity().getContentResolver(),
                        QS_SHOW_BRIGHTNESS, 0, UserHandle.USER_CURRENT);
            }
            updateDependencies(preference, value);
            return true;
        } else if (preference == mQSSlider) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    QS_SHOW_BRIGHTNESS, value ? 1 : 0, UserHandle.USER_CURRENT);
            if (value) {
                Settings.System.putIntForUser(getActivity().getContentResolver(),
                        QS_BRIGHTNESS_SLIDER_FOOTER, 0, UserHandle.USER_CURRENT);
            }
            updateDependencies(preference, value);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.PIXELDUST;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.pixeldust_settings_quicksettings;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
