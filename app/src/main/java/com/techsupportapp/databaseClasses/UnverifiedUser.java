package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий регистрационные данные неподтвержденного пользователя.
 * Основной {@link #UnverifiedUser(String branchId, String login, String password, int role,
        String userId, String userName, String workPlace, boolean isBlocked) конструктор}.
 * @author Monarch
 */
public class UnverifiedUser {

    //region Fields

    /**
     * Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    private String branchId;

    /**
     * Логин неподтвержденного пользователя.
     */
    private String login;

    /**
     * Уникальный идентификатор пользователя.
     */
    private String userId;

    /**
     * Имя пользователя.
     */
    private String userName;

    /**
     * Рабочее место пользователя.
     */
    private String workPlace;

    /**
     * Флаг, показывающий, заблокирован ли пользователь.
     */
    private boolean isBlocked;

    /**
     * Пароль неподтвержденного пользователя.
     */
    private String password;

    /**
     * Флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     */
    private int role;

    /**
     * Дата создания аккаунта для подтверждения.
     */
    private String registrationDate;

    //endregion

    //region Constructors

    /**
     * Конструктор по-умолчанию.
     * Используется для восстановления данных из базы данных.
     */
    public UnverifiedUser() {

    }

    /**
     * Конструктор, использующийся для добавления новых неподтвержденных пользователей в систему.
     * @param branchId Задает идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     * @param login Задает логин неподтвержденного пользователя.
     * @param password Задает пароль неподтвержденного пользователя.
     * @param role Задает флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     * @param userId
     * @param userName
     * @param workPlace
     * @param isBlocked
     */
    public UnverifiedUser(String branchId, String login, String password, int role,
                          String userId, String userName, String workPlace, boolean isBlocked) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.role = role;

        this.userId = userId;
        this.userName = userName;
        this.workPlace = workPlace;
        this.isBlocked = isBlocked;

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.registrationDate = formatter.format(Calendar.getInstance().getTime());
    }

    //endregion

    //region Getters

    /**
     * Метод, переводящий данного пользователя в категорию подтвержденных.
     * @return Подвержденного пользователя.
     */
    public User verifyUser() throws Exception {
        return new User(this.branchId, this.login, this.password, this.role, this.branchId,
                this.userName, this.workPlace, this.isBlocked);
    }

    /**
     * @return Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * @return Логин неподтвержденного пользователя.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return Пароль неподтвержденного пользователя.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     */
    public int getRole() {
        return role;
    }

    /**
     * @return Дату создания аккаунта для подтверждения.
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    //endregion

}
