package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomListPreference;
import com.aero.control.helpers.Android.CustomPreference;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PreferenceHandler;
import com.aero.control.helpers.Util;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PlaceHolderFragment implements Preference.OnPreferenceChangeListener {

    private PreferenceCategory PrefCat;
    private PreferenceScreen root;

    private boolean showDialog = true;

    private CustomPreference mDynFSync, mWriteBackControl, mFsync, mKSMSettings;
    private CustomPreference mFSTrimToggle, mDalvikSettings, mRandomSettings;
    private CustomListPreference mIOScheduler, mReadAHead;
    private String mFileSystem;
    private MemoryDalvikFragment mMemoryDalvikFragment;

    private PreferenceHandler mIOSchedulerHandler;

    private static final String MEMORY_SETTINGS_CATEGORY = "memory_settings";
    private static final String IO_SETTINGS_CATEGORY = "io_scheduler_parameter";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.memory_fragment);
        setHasOptionsMenu(true);
        ArrayList<CharSequence> readaheadValues = new ArrayList<CharSequence>();
        String temp;

        root = this.getPreferenceScreen();
        final PreferenceCategory memorySettingsCategory =
                (PreferenceCategory) findPreference(MEMORY_SETTINGS_CATEGORY);

        final PreferenceCategory ioSettingsCategory =
                (PreferenceCategory) findPreference(IO_SETTINGS_CATEGORY);

        mDynFSync = new CustomPreference(getActivity());
        mDynFSync.setName("dynFsync");
        mDynFSync.setTitle(R.string.pref_dynamic_fsync);
        mDynFSync.setSummary(R.string.pref_dynamic_fsync_sum);
        mDynFSync.setLookUpDefault(FilePath.DYANMIC_FSYNC);
        mDynFSync.setOrder(15);
        memorySettingsCategory.addPreference(mDynFSync);

        if ("1".equals(AeroActivity.shell.getInfo(FilePath.DYANMIC_FSYNC))) {
            mDynFSync.setClicked(true);
            mDynFSync.setSummary(R.string.enabled);
        } else if ("0".equals(AeroActivity.shell.getInfo(FilePath.DYANMIC_FSYNC))) {
            mDynFSync.setClicked(false);
            mDynFSync.setSummary(R.string.disabled);
        } else {
            if (memorySettingsCategory != null)
                memorySettingsCategory.removePreference(mDynFSync);
        }

        // FSync Toggle;
        mFsync = new CustomPreference(getActivity());
        mFsync.setName("fsync");
        mFsync.setTitle(R.string.pref_fsync);
        mFsync.setSummary(R.string.pref_fsync_sum);
        mFsync.setLookUpDefault(FilePath.FSYNC);
        mFsync.setOrder(14);
        memorySettingsCategory.addPreference(mFsync);

        temp = AeroActivity.shell.getInfo(FilePath.FSYNC);

        if ("Y".equals(temp) || "1".equals(temp)) {
            mFsync.setClicked(true);
            mFsync.setSummary(R.string.enabled);
        } else if ("N".equals(temp) || "0".equals(temp)) {
            mFsync.setClicked(false);
            mFsync.setSummary(R.string.disabled);
        } else {
            if (memorySettingsCategory != null)
                memorySettingsCategory.removePreference(mFsync);
        }

        mKSMSettings = new CustomPreference(getActivity());
        mKSMSettings.setName("ksm");
        mKSMSettings.setTitle(R.string.pref_ksm);
        mKSMSettings.setSummary(R.string.pref_ksm_sum);
        mKSMSettings.setLookUpDefault(FilePath.KSM_SETTINGS);
        mKSMSettings.setOrder(16);
        memorySettingsCategory.addPreference(mKSMSettings);

        temp = AeroActivity.shell.getInfo(FilePath.KSM_SETTINGS);

        if ("1".equals(temp)) {
            mKSMSettings.setClicked(true);
            mKSMSettings.setSummary(R.string.enabled);
        } else if ("2".equals(temp) || "0".equals(temp)) {
            mKSMSettings.setClicked(false);
            mKSMSettings.setSummary(R.string.disabled);
        } else {
            if (memorySettingsCategory != null)
                memorySettingsCategory.removePreference(mKSMSettings);
        }

        mWriteBackControl = new CustomPreference(getActivity());
        mWriteBackControl.setName("writeback");
        mWriteBackControl.setTitle(R.string.pref_dynamic_writeback);
        mWriteBackControl.setSummary(R.string.pref_dynamic_writeback_sum);
        mWriteBackControl.setLookUpDefault(FilePath.WRITEBACK);
        mWriteBackControl.setOrder(20);
        memorySettingsCategory.addPreference(mWriteBackControl);


        if ("1".equals(AeroActivity.shell.getInfo(FilePath.WRITEBACK))) {
            mWriteBackControl.setClicked(true);
            mWriteBackControl.setSummary(R.string.enabled);
        } else if ("0".equals(AeroActivity.shell.getInfo(FilePath.WRITEBACK))) {
            mWriteBackControl.setClicked(false);
            mWriteBackControl.setSummary(R.string.disabled);
        } else {
            if (memorySettingsCategory != null)
                memorySettingsCategory.removePreference(mWriteBackControl);
        }

        for (int i = 1; i <= 32; i++) {
            readaheadValues.add("" + (128 * i));
        }

        mReadAHead = new CustomListPreference(getActivity());
        mReadAHead.setName("read_ahead");
        mReadAHead.setOrder(12);
        mReadAHead.setTitle(R.string.pref_readahead);
        mReadAHead.setDialogTitle(R.string.pref_readahead_dialog);
        mReadAHead.setEntries(readaheadValues.toArray(new CharSequence[0]));
        mReadAHead.setEntryValues(readaheadValues.toArray(new CharSequence[0]));
        mReadAHead.setValue(AeroActivity.shell.getInfo(FilePath.READAHEAD_PARAMETER));
        mReadAHead.setSummary(AeroActivity.shell.getInfo(FilePath.READAHEAD_PARAMETER));
        mReadAHead.setOnPreferenceChangeListener(this);
        memorySettingsCategory.addPreference(mReadAHead);

        mRandomSettings = (CustomPreference)findPreference("entropy_settings");
        mRandomSettings.setOrder(22);

        mFSTrimToggle = (CustomPreference)findPreference("fstrim_toggle");
        mFSTrimToggle.setOrder(25);
        mFSTrimToggle.setHideOnBoot(true);
        mDalvikSettings = (CustomPreference)findPreference("dalvik_settings");
        mDalvikSettings.setOrder(30);
        mDalvikSettings.setHideOnBoot(true);

        mIOScheduler = new CustomListPreference(getActivity());
        mIOScheduler.setName("io_scheduler_list");
        mIOScheduler.setTitle(R.string.io_scheduler);
        mIOScheduler.setDialogTitle(R.string.io_scheduler);
        mIOScheduler.setEntries(AeroActivity.shell.getInfoArray(FilePath.GOV_IO_FILE, 0, 1));
        mIOScheduler.setEntryValues(AeroActivity.shell.getInfoArray(FilePath.GOV_IO_FILE, 0, 1));
        mIOScheduler.setValue(AeroActivity.shell.getInfoString(AeroActivity.shell.getInfo(FilePath.GOV_IO_FILE)));
        mIOScheduler.setSummary(AeroActivity.shell.getInfoString(AeroActivity.shell.getInfo(FilePath.GOV_IO_FILE)));
        mIOScheduler.setDialogIcon(R.drawable.device_drive);
        mIOScheduler.setOnPreferenceChangeListener(this);
        ioSettingsCategory.addPreference(mIOScheduler);

        mIOSchedulerHandler = new PreferenceHandler(getActivity(), ioSettingsCategory, getPreferenceManager());
        mIOSchedulerHandler.addInvisiblePreference();

        if (showDialog) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    // Ensure only devices with this special path are checked;
                    final String fileMount[] = AeroActivity.shell.getInfo("/proc/mounts", false);
                    boolean fileMountCheck = false;

                    for (String tmp : fileMount) {
                        if (tmp.contains("/dev/block/mmcblk1p25")) {
                            fileMountCheck = true;
                            break;
                        }
                    }

                    showDialog = false;

                    if (fileMountCheck) {
                        final String fileJournal = AeroActivity.shell.getRootInfo("tune2fs -l", "/dev/block/mmcblk1p25");
                        final boolean fileSystemCheck = fileJournal.length() != 0 && fileJournal.contains("has_journal");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!fileSystemCheck) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    // Just reuse aboutScreen, because its Linear and has a TextView
                                    View layout = inflater.inflate(R.layout.about_screen, null);
                                    TextView aboutText = (TextView) (layout != null ? layout.findViewById(R.id.aboutScreen) : null);
                                    builder.setTitle(R.string.has_journal_dialog_header);
                                    if (aboutText != null) {
                                        aboutText.setText(getText(R.string.has_journal_dialog));
                                        aboutText.setTextSize(13);
                                    }
                                    builder.setView(layout)
                                            .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                }
                                            });
                                    builder.show();
                                }
                            }
                        });
                    }
                }
            };
            Thread checkThread = new Thread(runnable);
            checkThread.start();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // Set up our file;
        int output = 0;

        if (AeroActivity.genHelper.doesExist(getActivity().getFilesDir().getAbsolutePath() + "/" + FilePath.FILENAME)) {
            output = 1;
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_memory_fragment_trim, R.string.showcase_memory_fragment_trim_sum, FilePath.FILENAME);

    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.memory_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_io_settings:
                loadIOParameter();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        CustomPreference cusPref = null;

        if (preference == mDynFSync) {

            mDynFSync.setClicked(!mDynFSync.isClicked());

            if (mDynFSync.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.DYANMIC_FSYNC);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.DYANMIC_FSYNC);

            cusPref = (CustomPreference) preference;

        } else if (preference == mFsync) {

            mFsync.setClicked(!mFsync.isClicked());

            if (mFsync.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.FSYNC);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.FSYNC);

            cusPref = (CustomPreference) preference;

        } else if (preference == mKSMSettings) {

            mKSMSettings.setClicked(!mKSMSettings.isClicked());

            if (mKSMSettings.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.KSM_SETTINGS);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.KSM_SETTINGS);

            cusPref = (CustomPreference) preference;

        } else if (preference == mWriteBackControl) {

            mWriteBackControl.setClicked(!mWriteBackControl.isClicked());

            if (mWriteBackControl.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.WRITEBACK);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.WRITEBACK);

            cusPref = (CustomPreference)preference;

        } else if (preference == mFSTrimToggle) {
            fsTrimToggleClick();
        } else if (preference == mDalvikSettings) {

            if (mMemoryDalvikFragment == null)
                mMemoryDalvikFragment = new MemoryDalvikFragment();

            AeroActivity.mHandler.postDelayed(new Runnable()  {
                @Override
                public void run() {
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .replace(R.id.content_frame, mMemoryDalvikFragment)
                            .addToBackStack("Memory")
                            .commit();
                }
            },AeroActivity.genHelper.getDefaultDelay());
        } else if (preference == mRandomSettings) {
            onRandomClick();
        }

        // If its checked, we want to save it;
        if (cusPref != null) {
            if (cusPref.isChecked()) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPrefs.edit();
                String state = cusPref.isClicked() ? "1" : "0";
                editor.putString(cusPref.getName(), state).commit();
            }
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String value = (String) newValue;

        if (preference == mIOScheduler) {
            mIOScheduler.setSummary(value);
            AeroActivity.shell.setRootInfo(value, FilePath.GOV_IO_FILE);

            // Kill everything with fire;
            if (PrefCat != null)
                root.removePreference(PrefCat);
        } else if (preference == mReadAHead) {
            AeroActivity.shell.setRootInfo(value, FilePath.READAHEAD_PARAMETER);
            mReadAHead.setSummary(value);
        } else {
            return false;
        }
        return true;
    }

    private void onRandomClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate our layout and build up the UI;
        View layout = inflater.inflate(R.layout.memory_random, null);

        TextView txtRandomRead = (TextView) layout.findViewById(R.id.random_read);
        TextView txtRandomWrite = (TextView) layout.findViewById(R.id.random_write);

        final EditText editRandomRead = (EditText) layout.findViewById(R.id.random_read_value);
        final EditText editRandomWrite = (EditText) layout.findViewById(R.id.random_write_value);

        txtRandomRead.setText(Util.getLastSysValue(FilePath.RANDOM_READ_WAKEUP));
        txtRandomWrite.setText(Util.getLastSysValue(FilePath.RANDOM_WRITE_WAKEUP));

        editRandomRead.setText(AeroActivity.shell.getFastInfo(FilePath.RANDOM_READ_WAKEUP));
        editRandomWrite.setText(AeroActivity.shell.getFastInfo(FilePath.RANDOM_WRITE_WAKEUP));

        builder.setIcon(R.drawable.puzzle);
        // Display the current available entropy as well as the pool size;
        builder.setTitle(getText(R.string.pref_entropy_settings) + " (" + AeroActivity.shell.getFastInfo(FilePath.RANDOM_POOL_AVAIL) + "/" +
                            AeroActivity.shell.getFastInfo(FilePath.RANDOM_POOL_SIZE) + ")");
        builder.setView(layout);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Build up our commands;
                String[] cmds = {
                  "echo " + editRandomRead.getText() + " > " + FilePath.RANDOM_READ_WAKEUP,
                  "echo " + editRandomWrite.getText() + " > " + FilePath.RANDOM_WRITE_WAKEUP,
                };

                AeroActivity.shell.setRootInfo(cmds);

                if (mRandomSettings.isChecked()) {
                    SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                    // We still want to write it here, since we can disable it later;
                    preference.edit().putStringSet(mRandomSettings.getKey(), new HashSet<String>(Arrays.asList(cmds))).commit();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.show();

    }

    private void fsTrimToggleClick() {

        // If the library doesn't exist, skip through;
        if (!(AeroActivity.genHelper.doesExist("/system/xbin/fstrim"))) {
            Toast.makeText(getActivity(), R.string.pref_fstrim_no_busybox, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mFileSystem == null)
            mFileSystem = AeroActivity.shell.getRootInfo("mount", "");

        final CharSequence[] system = {" /system ", " /data ", " /cache "};
        final ArrayList<String> fs = new ArrayList<String>();

        int tmp;
        int count = 0;
        String temp;

        for (CharSequence a : system) {
            if (mFileSystem.contains(a)) {
                tmp = mFileSystem.indexOf(a.toString());
                temp = mFileSystem.substring(tmp, tmp + a.length() + 4).replace(a, "");

                if (temp.equals("ext3") || temp.equals("ext4")) {
                    fs.add(a.toString());
                    count++;
                }
            }
        }
        final CharSequence[] fsystem = fs.toArray(new CharSequence[0]);

        // If the device doesn't support trimable filesystems;
        if (count == 0) {
            Toast.makeText(getActivity(), R.string.unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ProgressDialog update = new ProgressDialog(getActivity());
        builder.setTitle(R.string.pref_fstrim);
        builder.setIcon(R.drawable.file_exe);
        builder.setItems(fsystem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final String b = (String)fsystem[item];
                update.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                update.setCancelable(false);
                update.setIndeterminate(true);
                update.setIndeterminateDrawable(getResources().getDrawable(R.drawable.spinner_animation));
                update.setMessage(Util.getRandomLoadingText(getActivity()));
                update.show();
                AeroActivity.shell.remountSystem();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Set up the root-command;
                            AeroActivity.shell.getRootInfo("fstrim -v", b);
                            // Sleep the current thread and exit dialog;
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            Log.e("Aero", "An error occurred while trimming.", e);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update.dismiss();
                            }
                        });
                    }
                };
                Thread trimThread = new Thread(runnable);
                if (!trimThread.isAlive())
                    trimThread.start();
            }
        }).show();
    }

    public void DrawFirstStart(int header, int content, String filename) {

        try {
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        Target homeTarget = new Target() {
            @Override
            public Point getPoint() {
                return new Point(200, 200);
            }
        };

        new ShowcaseView.Builder(getActivity())
                .setContentTitle(header)
                .setContentText(content)
                .setTarget(homeTarget)
                .build();
    }

    private void loadIOParameter() {

        // First remove our invisible element;
        mIOSchedulerHandler.removeInvisiblePreference();

        try {
            String completeParamterList[] = AeroActivity.shell.getDirInfo(FilePath.GOV_IO_PARAMETER, true);

            // If there are already some entries, kill them all (with fire)
            if (PrefCat != null)
                root.removePreference(PrefCat);

            if (completeParamterList.length == 0) {
                Toast.makeText(getActivity(), R.string.pref_io_scheduler_no_parameter, Toast.LENGTH_LONG).show();
                return;
            }

            PrefCat = new PreferenceCategory(getActivity());
            PrefCat.setTitle(R.string.pref_io_scheduler);
            root.addPreference(PrefCat);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("Aero", "Something interrupted the main Thread, try again.", e);
            }

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, FilePath.GOV_IO_PARAMETER);

            // Probably the wrong place, should be in getDirInfo ?
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), R.string.pref_io_scheduler_no_parameter, Toast.LENGTH_LONG).show();
            root.removePreference(PrefCat);
            Log.e("Aero", "There isn't any folder i can check. Does this governor has parameters?", e);
        }
    }
}

