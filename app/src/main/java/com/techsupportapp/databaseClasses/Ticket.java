package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий данные заявки пользователя.
 * Основной {@link #Ticket(String, String, String, String) конструктор}.
 * @author Monarch
 */
public class Ticket {

    //region Constants

    /**
     * Новая, не рассмотренная заявка.
     */
    final static int NOT_ACCEPTED = 0;

    /**
     * Заявка, находящаяся на рассмотрении у консультанта.
     */
    final static int ACCEPTED = 1;

    /**
     * Заявка, переданная для других консультантов.
     */
    final static int SUBMITTED = 2;

    /**
     * Заявка, решение которой было подтверждено пользователем - создателем заявки.
     */
    final static int CONFIRMED_BY_USER = 4;

    /**
     * Решенная заявка.
     */
    final static int SOLVED = 8;

    //endregion

    //region Fields

    /**
     * Состояние заявки.
     */
    private int ticketState;

    /**
     * Идентификатор заявки.
     */
    private String ticketId;

    /**
     * Идентификатор пользователя - создателя заявки.
     */
    private String userId;

    /**
     * Идентификатор администратора отвечающего за решение заявки.
     */
    private String adminId;

    /**
     * Тема заявки.
     */
    private String topic;

    /**
     * Сообщение - описание проблемы в заявке.
     */
    private String message;

    /**
     * Дата создания заявки.
     */
    private String createDate;

    //endregion

    /**
     * Конструктор по-умолчанию.
     */
    public Ticket() {

    }

    /**
     * Конструктор, использующийся для добавления новых заявок пользователей в систему.
     * @param ticketId Идентификатор заявки.
     * @param userId Идентификатор пользователя - создателя заявки.
     * @param topic Тема заявки.
     * @param message Сообщение - описание проблемы в заявке.
     */
    public Ticket(String ticketId, String userId, String topic, String message) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.topic = topic;
        this.message = message;
        this.adminId = null;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.createDate = formatter.format(Calendar.getInstance().getTime());
        this.ticketState = Ticket.NOT_ACCEPTED;
    }

    /**
     * Метод, задающий администратора, который будет решать проблему.
     * @param adminId
     */
    public void addAdmin(String adminId) {
        this.adminId = adminId;
        this.ticketState = Ticket.ACCEPTED;
    }

    /**
     * @return Идентификатор заявки.
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * @return Идентификатор пользователя - создателя заявки.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Идентификатор администратора отвечающего за решение заявки.
     */
    public String getAdminId() {
        return adminId;
    }

    /**
     * @return Тему заявки.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return Сообщение - описание проблемы в заявке.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Дату создания заявки.
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * @return Состояние заявки.
     */
    public int getTicketState() {
        return ticketState;
    }

}
