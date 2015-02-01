package com.ventplan.desktop

/**
 * WAC-161: Zuletzt geöffnete Projekte
 * Save and load preferences for a Most Recently Used (MRU) list.
 */
class MRUFileManager {

    public static final String DEFAULT_NODE = "/com/ventplan/desktop/mru"

    public static final int DEFAULT_MAX_SIZE = 10;
    private int currentMaxSize = 0;
    private LinkedList mruFileList;

    private static MRUFileManager INSTANCE = new MRUFileManager();

    public static MRUFileManager getInstance() {
        return INSTANCE;
    }

    private MRUFileManager() {
        load();
        setMaxSize(DEFAULT_MAX_SIZE);
    }

    public static String getPrefValue(int i) {
        return PrefHelper.getPrefValue(DEFAULT_NODE, '' + i)
    }

    /**
     * Saves a list of MRU files out to a file.
     */
    public void save() {
        try {
            // Remove node and save the new list...
            PrefHelper.getPrefs(DEFAULT_NODE).removeNode();
            for (int i = 0; i < mruFileList.size(); i++) {
                if (i < DEFAULT_MAX_SIZE) {
                    def f = mruFileList.get(i)
                    if (f instanceof java.io.File) {
                        PrefHelper.setPrefValue(DEFAULT_NODE, '' + i, f.getAbsolutePath());
                    } else {
                        PrefHelper.setPrefValue(DEFAULT_NODE, '' + i, (String) f);
                    }
                }
            }
        } catch (Exception e) {
            println e
        }
    }

    /**
     * Gets the size of the MRU file list.
     */
    public int size() {
        return mruFileList.size();
    }

    /**
     * Gets the list of files stored in the MRU file list.
     */
    public String[] getMRUFileList() {
        if (size() == 0) {
            return null;
        }
        String[] ss = new String[size()];
        for (int i = 0; i < size(); i++) {
            String s = getPrefValue(i);
            if (null != s) {
                ss[i] = s;
            }
        }
        return ss;
    }

    /**
     * Moves the the index to the top of the MRU List
     * @param index The index to be first in the mru list
     */
    public void moveToTop(int index) {
        def o = mruFileList.remove(index);
        mruFileList.addFirst(o);
    }

    /**
     * Adds an object to the mru.
     * @param f
     */
    protected void setMRU(File f) {
        if (null != f) { // java.lang.NullPointerException: Cannot invoke method getAbsolutePath() on null object
            setMRU(f.getAbsolutePath())
        }
    }

    /**
     * Adds an object to the mru.
     * @param s
     */
    protected void setMRU(String s) {
        def file = new java.io.File(s.toString())
        if (file.exists()) {
            def contains = false
            mruFileList.each {
                if (it.equals(s)) {
                    contains = true
                }
            }
            if (!contains) {
                mruFileList.addFirst(s);
                setMaxSize(DEFAULT_MAX_SIZE);
            } else {
                int index = mruFileList.indexOf(s);
                moveToTop(index);
            }
        } else {
            // ignore
        }
    }

    /**
     * Loads the MRU file list in from a file and stores it in a LinkedList.
     * If no file exists, a new LinkedList is created.
     */
    protected void load() {
        if (null == mruFileList) {
            mruFileList = new LinkedList();
        }
        for (int i = 0; i < DEFAULT_MAX_SIZE; i++) {
            try {
                String value = getPrefValue(i);
                if (value) {
                    setMRU(value);
                }
            } catch (Exception e) {
                // key is not in prefs...
                break
            }
        }

    }

    /**
     * Ensures that the MRU list will have a MaxSize.
     */
    protected void setMaxSize(int maxSize) {
        if (maxSize < mruFileList.size()) {
            for (int i = 0; i < mruFileList.size() - maxSize; i++) {
                mruFileList.removeLast();
            }
        }
        currentMaxSize = maxSize;
    }

}
