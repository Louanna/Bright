package com.movit.platform.common.module.user.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationBean;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UserDao {

    private final String TABLE_USER = "User";
    private final String TABLE_ORG = "Orgunit";

    private final String PACKAGE_NAME; // 这个是自己项目包路径
    private final String DATABASE_PATH;// 获取存储位置地址
    public final static String DATABASE_FILENAME = "eoop.db"; // 这个是DB文件名字

    private static UserDao manager;

    private UserDao(Context context) {
        PACKAGE_NAME = context.getPackageName();
        DATABASE_PATH = "/data"
                + Environment.getDataDirectory().getAbsolutePath() + "/"
                + PACKAGE_NAME + "/databases";
        try {
            copyDBFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserDao getInstance(Context mContext) {
        if (manager == null) {
            manager = new UserDao(mContext);
        }
        return manager;
    }

    private void copyDBFile() throws IOException {
        File dir = new File(DATABASE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (!(new File(getUserDBFile())).exists()) {
            InputStream is = new FileInputStream(CommConstants.SD_DOWNLOAD
                    + getUserDBFileName());

            FileOutputStream fos = new FileOutputStream(getUserDBFile());
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        }

        SQLiteDatabase database = openDatabase();
        database.close();
    }

    public String getUserDBPath() {
        return DATABASE_PATH;
    }

    public String getUserDBFile() {
        return DATABASE_PATH + "/" + DATABASE_FILENAME;
    }

    public String getUserDBFileName() {
        return DATABASE_FILENAME;
    }

    public SQLiteDatabase openDatabase() {
        try {
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            return SQLiteDatabase.openOrCreateDatabase(databaseFilename,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<OrganizationTree> getAllOrgunitions() {
        SQLiteDatabase database = openDatabase();
        Cursor c = database
                .query(TABLE_ORG, null, "orgId != ?", new String[]{"000000000000000000000000000000000001"}, null, null, null);
        ArrayList<OrganizationTree> nodes = new ArrayList<OrganizationTree>();
        while (c.moveToNext()) {
            String orgId = c.getString(c.getColumnIndex("orgId"));
            String parentId = c.getString(c.getColumnIndex("parentId"));
            String objname = c.getString(c.getColumnIndex("objName"));
            OrganizationTree org = new OrganizationTree();
            org.setId(orgId);
            org.setParentId(parentId);
            org.setObjname(objname);
            nodes.add(org);
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }


    public ArrayList<OrganizationBean> getAllUserWithOrganization() {
        SQLiteDatabase database = openDatabase();

//        String sqlStr = "SELECT * FROM " + TABLE_USER + " Left JOIN "
//                + TABLE_ORG + " ON User.orgId = Orgunit.orgId where User.orgId != 'c523986306614017ba56b0121fe29235' order by User.firstNameSpell COLLATE NOCASE";

        String sqlStr = "select a.objName as depName,b.* from Orgunit a,(select User.*,Orgunit.objName,Orgunit.parentId from User, Orgunit where User.orgId = Orgunit.orgId and User.orgId != '000000000000000000000000000000000001') b where a.orgId = b.parentId order by b.firstNameSpell COLLATE NOCASE";

        Cursor c = database.rawQuery(sqlStr, null);
        ArrayList<OrganizationBean> nodes = new ArrayList<>();
        while (c.moveToNext()) {
            String objname = c.getString(c.getColumnIndex("objName"));
            OrganizationBean bean = new OrganizationBean(objname, convertUserInfo(c));
            bean.setDepName(c.getString(c.getColumnIndex("depName")));
            nodes.add(bean);
        }
        if (null != c) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }

    public ArrayList<UserInfo> getAllUserInfos() {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + TABLE_USER + " where " +
                "orgId != '000000000000000000000000000000000001' and orgId != '000000000000000000000000000000000000' " +
                "order by sort desc,firstNameSpell COLLATE NOCASE asc", null);
        ArrayList<UserInfo> nodes = new ArrayList<>();
        while (c.moveToNext()) {
            nodes.add(convertUserInfo(c));
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }

    public OrganizationTree getOrganizationByName(String objName) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_ORG, null, "objName = ?",
                new String[]{objName}, null, null, null);
        if (c.moveToNext()) {
            OrganizationTree org = new OrganizationTree();
            String orgId = c.getString(c.getColumnIndex("orgId"));
            String parentId = c.getString(c.getColumnIndex("parentId"));
            String objname = c.getString(c.getColumnIndex("objName"));
            org.setObjname(objname);
            org.setId(orgId);
            org.setParentId(parentId);
            if (c != null) {
                c.close();
            }
            if (null != database) {
                database.close();
            }
            return org;
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return null;
    }

    public OrganizationTree getOrganizationByOrgId(String orgid) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_ORG, null, "orgId = ?",
                new String[]{orgid}, null, null, null);
        if (c.moveToNext()) {
            OrganizationTree org = new OrganizationTree();
            String orgId = c.getString(c.getColumnIndex("orgId"));
            String parentId = c.getString(c.getColumnIndex("parentId"));
            String objname = c.getString(c.getColumnIndex("objName"));
            org.setObjname(objname);
            org.setId(orgId);
            org.setParentId(parentId);
            if (c != null) {
                c.close();
            }
            if (null != database) {
                database.close();
            }
            return org;
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return null;
    }

    public ArrayList<OrganizationTree> getAllOrganizationsByParentId(String id) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_ORG, null, "parentId = ?",
                new String[]{id}, null, null, null);
        ArrayList<OrganizationTree> nodes = new ArrayList<OrganizationTree>();
        while (c.moveToNext()) {
            OrganizationTree org = new OrganizationTree();
            String orgId = c.getString(c.getColumnIndex("orgId"));
            String parentId = c.getString(c.getColumnIndex("parentId"));
            String objname = c.getString(c.getColumnIndex("objName"));
            org.setObjname(objname);
            org.setId(orgId);
            org.setParentId(parentId);
            nodes.add(org);
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }

    public ArrayList<UserInfo> getAllUserInfosByOrgId(String orgId) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_USER, null, "orgId = ?",
                new String[]{orgId}, null, null, "sort desc,firstNameSpell asc");
        ArrayList<UserInfo> nodes = new ArrayList<>();
        while (c.moveToNext()) {
            nodes.add(convertUserInfo(c));
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }

    public UserInfo getUserInfoById(String id) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_USER, null, "userId = ?",
                new String[]{id}, null, null, null);
        if (c.moveToNext()) {
            UserInfo userInfo = convertUserInfo(c);
            if (c != null) {
                c.close();
            }
            if (null != database) {
                database.close();
            }
            return userInfo;
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return null;
    }

    public ArrayList<UserInfo> getAllUserInfosBySearch(String search) {
        String sql = "SELECT * FROM "
                + TABLE_USER
                + " where empAdname like ? or empCname like ? or fullNameSpell like ? or firstNameSpell like ? " +
                "or phone like ? or mphone like ? COLLATE NOCASE";
        SQLiteDatabase database = openDatabase();
        Cursor c = database.rawQuery(sql, new String[]{"%" + search + "%",
                "%" + search + "%", "%" + search + "%", "%" + search + "%",
                "%" + search + "%", "%" + search + "%"});
        ArrayList<UserInfo> nodes = new ArrayList<UserInfo>();
        while (c.moveToNext()) {
            UserInfo userInfo = convertUserInfo(c);
            if (!"000000000000000000000000000000000001".equals(userInfo.getOrgId())) {
                nodes.add(userInfo);
            }
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return nodes;
    }

    public UserInfo getUserInfoByAddress(String address) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_USER, null, "mail = ?",
                new String[]{address}, null, null, null);
        UserInfo userInfo = null;
        while (c.moveToNext()) {
            userInfo = convertUserInfo(c);
        }

        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }

        return userInfo;
    }

    public UserInfo getUserInfoByADName(String adName) {
        SQLiteDatabase database = openDatabase();
        Cursor c = database.query(TABLE_USER, null,
                "empAdname = ? COLLATE NOCASE", new String[]{adName}, null,
                null, null);
        if (c.moveToNext()) {
            UserInfo userInfo = convertUserInfo(c);
            if (c != null) {
                c.close();
            }
            if (null != database) {
                database.close();
            }
            return userInfo;
        }
        if (c != null) {
            c.close();
        }
        if (null != database) {
            database.close();
        }
        return null;
    }

    private UserInfo convertUserInfo(Cursor c) {
        UserInfo userInfo = new UserInfo();
        String userId = c.getString(c.getColumnIndex("userId"));
        String empId = c.getString(c.getColumnIndex("empId"));
        String empAdname = c.getString(c.getColumnIndex("empAdname"));
        String empCname = c.getString(c.getColumnIndex("empCname"));
        String avatar = c.getString(c.getColumnIndex("avatar"));
        String gender = c.getString(c.getColumnIndex("gender"));
        String phone = c.getString(c.getColumnIndex("phone"));
        String mphone = c.getString(c.getColumnIndex("mphone"));
        String mail = c.getString(c.getColumnIndex("mail"));
        String actype = c.getString(c.getColumnIndex("actype"));
        String orgId = c.getString(c.getColumnIndex("orgId"));
        String city = c.getString(c.getColumnIndex("cityName"));
        String is_open = c.getString(c.getColumnIndex("isOpen"));
        String user_open = c.getString(c.getColumnIndex("userOpen"));
        int sort = c.getInt(c.getColumnIndex("sort"));

        String fullNameSpell = c.getString(c.getColumnIndex("fullNameSpell"));
        String firstNameSpell = c.getString(c.getColumnIndex("firstNameSpell"));
        String position = c.getString(c.getColumnIndex("positionName"));

        userInfo.setId(userId);
        userInfo.setActype(actype);
        userInfo.setAvatar(avatar);
        userInfo.setEmpAdname(empAdname);
        userInfo.setEmpCname(empCname);
        userInfo.setEmpId(empId);
        userInfo.setGender(gender);
        userInfo.setMail(mail);
        userInfo.setMphone(mphone);
        userInfo.setOrgId(orgId);
        userInfo.setPhone(phone);
        userInfo.setCity(city);
        userInfo.setSort(sort);
        userInfo.setUserOpen(user_open);
        userInfo.setIsOpen(is_open);
        userInfo.setFullNameSpell(fullNameSpell);
        userInfo.setFirstNameSpell(firstNameSpell);
        userInfo.setPosition(position);
        return userInfo;
    }

    public void updateOrgByFlags(OrganizationTree org) {
        if ("create".equals(org.getDeltaFlag())) {
            replaceOrgunition(org);
        } else if ("delete".equals(org.getDeltaFlag())) {
            deleteOrg(org);
        } else if ("update".equals(org.getDeltaFlag())) {
            replaceOrgunition(org);
        }
    }

    public void updateUserByFlags(UserInfo user) {
        if ("create".equals(user.getDeltaFlag())) {
            replaceUser(user);
        } else if ("delete".equals(user.getDeltaFlag())) {
            deleteUser(user);
        } else if ("update".equals(user.getDeltaFlag())) {
            UserInfo userInfo = getUserInfoById(user.getId());
            if (null != userInfo) {
                updateUser(user);
            } else {
                replaceUser(user);
            }
        }
    }

    private int replaceOrgunition(OrganizationTree org) {
        SQLiteDatabase database = openDatabase();
        CommConstants.allOrgunits.remove(org);
        CommConstants.allOrgunits.add(org);
        ContentValues values = new ContentValues();
        values.put("orgId", org.getId());
        values.put("parentId", org.getParentId());
        values.put("objName", org.getObjname());
        long sid = database.replace(TABLE_ORG, null, values);
        if (null != database)
            database.close();
        return (int) sid;
    }

    private int replaceUser(UserInfo user) {
//		if(null!= CommConstants.allUserInfos){
//			CommConstants.allUserInfos.remove(user);
//		}else{
//			CommConstants.allUserInfos = new ArrayList<>();
//		}
//		CommConstants.allUserInfos.add(user);
        ContentValues values = new ContentValues();
        values.put("userId", user.getId());
        values.put("empId", user.getEmpId());
        values.put("empAdname", user.getEmpAdname());
        values.put("empCname", user.getEmpCname());
        values.put("avatar", user.getAvatar());
        values.put("gender", user.getGender());
        values.put("phone", user.getPhone());
        values.put("mphone", user.getMphone());
        values.put("mail", user.getMail());
        values.put("actype", user.getActype());
        values.put("orgId", user.getOrgId());
        values.put("cityName", user.getCity());
        values.put("sort", user.getSort());
        values.put("isOpen", user.getIsOpen());
        values.put("userOpen", user.getUserOpen());
        values.put("fullNameSpell", user.getFullNameSpell());
        values.put("firstNameSpell", user.getFirstNameSpell());
        SQLiteDatabase database = openDatabase();
        long sid = database.replace(TABLE_USER, null, values);
        if (null != database)
            database.close();
        return (int) sid;
    }

    private int updateUser(UserInfo user) {
        ContentValues values = new ContentValues();
        values.put("userId", user.getId());
        values.put("empId", user.getEmpId());
        values.put("empAdname", user.getEmpAdname());
        values.put("empCname", user.getEmpCname());
        values.put("avatar", user.getAvatar());
        values.put("gender", user.getGender());
        values.put("phone", user.getPhone());
        values.put("mphone", user.getMphone());
        values.put("mail", user.getMail());
        values.put("actype", user.getActype());
        values.put("orgId", user.getOrgId());
        values.put("cityName", user.getCity());
        values.put("sort", user.getSort());
        values.put("isOpen", user.getIsOpen());
        values.put("userOpen", user.getUserOpen());
        values.put("fullNameSpell", user.getFullNameSpell());
        values.put("firstNameSpell", user.getFirstNameSpell());
        SQLiteDatabase database = openDatabase();
        long sid = database.update(TABLE_USER, values, "userId = ?", new String[]{user.getId()});
        if (null != database)
            database.close();
        return (int) sid;
    }

    public void deleteUser(UserInfo user) {
        CommConstants.allUserInfos.remove(user);
        SQLiteDatabase database = openDatabase();
        database.delete(TABLE_USER, "userId = ?", new String[]{user.getId()});
        if (null != database)
            database.close();
    }

    public void deleteOrg(OrganizationTree orgu) {
        CommConstants.allOrgunits.remove(orgu);
        SQLiteDatabase database = openDatabase();
        database.delete(TABLE_ORG, "orgId = ?", new String[]{orgu.getId()});
        if (null != database)
            database.close();
    }

    /**
     * 关闭数据库
     */
    public void closeDb(SQLiteDatabase database) {
        database.close();
    }

    private SQLiteDatabase database;

    public Cursor execSQLForCursor(String sql) {
        database = openDatabase();
        Cursor c = database.rawQuery(sql, null);
        return c;
    }

    public void closeDb() {
        if (null != database) {
            database.close();
        }
    }

    public void deleteDb() {
        String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
        File file = new File(databaseFilename);
        if (file.exists()) {
            file.delete();
        }
    }

}
