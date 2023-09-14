package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    // регулярка для определения даты-времени в сообщении
    private final Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");

    // паттерн форматирования текста в дату-время
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
            updates.forEach(update -> {
//                Matcher matcher = pattern.matcher(update.message().text());
                // информация об апдейтах
                logger.info("Processing update: {}", update);
                // записываю данные: сообщение и айди чата
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();
                // проверяю на /start
                try {
                    if (messageText.equals("/start")) {
                        // отправляю приветствие
                        sendStartMessage(chatId, update.message().chat().firstName());
                    } else {
                        // обрабатываю входящее сообщение (уведомление об ошибке/ уведомление о создании напоминания)
                        sendNotification(chatId, messageText);
                    }
                } catch (NullPointerException e) {
                    logger.warn("Получены некорректные данные в чате " + chatId);
                    sendMessage(chatId, "Извини, я тебя не понимаю, попробуй еще раз");
                }
            });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendStartMessage(Long chatId, String name) {
        // создаю и отправляю приветственное сообщение
        String startMessage = "Привет, " + name + "! Чтобы добавить напоминание, отправь сообщение вида *ДД.ММ.ГГГГ ЧЧ:ММ НАПОМИНАНИЕ*";
        sendMessage(chatId, startMessage);
        logger.info("Приветственное сообщение отправлено в чат " + chatId);
    }
    private void sendMessage(Long chatId, String message) {
        // создаю сущность сообщения
        SendMessage sendMessage = new SendMessage(chatId, message);
        // создаю сущность ответа и и отправляю его
        SendResponse response = telegramBot.execute(sendMessage);
    }

    private void sendNotification(Long chatId, String messageText) {
        Matcher matcher = pattern.matcher(messageText);
        try {
            // проверяю, совпадает ли сообщение с форматом даты-времени и напоминания
            if (matcher.matches()) {
                // создаю сущность "напоминание" и присваиваю ей значения, которые получил в сообщении пользователя
                String notificationText = matcher.group(3);
                LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), dateTimeFormatter);
                // сохраняю напоминание в БД
                notificationTaskRepository.save(new NotificationTask(chatId, notificationText, dateTime));
                // отправляю сообщение пользователю
                sendMessage(chatId, "Понял-принял, все напомню!");
                logger.info("Напоминание сохранено для чата " + chatId);
            } else {
                sendMessage(chatId, "Извини, я тебя не понимаю, попробуй еще раз");
                logger.warn("Получены некорректные данные в чате " + chatId);
            }
        } catch (DateTimeParseException e) {
            logger.warn("Получены некорректные данные в чате " + chatId);
            sendMessage(chatId, " Отправь реальную дату");
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void findAndSendNotification() {
        // создаю список всех записей, у которых время напоминания СЕЙЧАС
        List<NotificationTask> notifications = new ArrayList<>(notificationTaskRepository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
        // рассылаю уведомления по всем чатам из списка
        notifications.forEach(notificationTask -> {
            sendMessage(notificationTask.getChatId(), notificationTask.getNotification());
            logger.info("Отправлено напоминание для чата " + notificationTask.getChatId());
        });
    }
}
