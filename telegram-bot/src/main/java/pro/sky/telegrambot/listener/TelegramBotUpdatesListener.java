package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");

    // паттерн форматирования текста в дату-время
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
//        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
            updates.forEach(update -> {
                Matcher matcher = pattern.matcher(update.message().text());
                logger.info("Processing update: {}", update);
                // Process your updates here

                // проверяю, приходит ли команда старт
                if (update.message().text().equals("/start")) {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

                    // запоминаю айди чата, создаю сообщение и отправляю приветствие
                    Long chatId = update.message().chat().id();
                    SendMessage message = new SendMessage(chatId, "Привет! Чтобы добавить напоминание, отправь сообщение вида *ДД.ММ.ГГГГ ЧЧ:ММ НАПОМИНАНИЕ*");
                    SendResponse response = telegramBot.execute(message);

                // проверяю, совпадает ли сообщение с форматом даты-времени и напоминания
                // TODO добавить обработку исключения, когда на вход поступает нужный нам формат, но вне реалистичных пределов значения (99.99.0000 99:99 например)
                } else if (matcher.matches()) {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    Long chatId = update.message().chat().id();
                    SendMessage message = new SendMessage(chatId, "Понял-принял, все напомню!");
                    SendResponse response = telegramBot.execute(message);

                    // создаю сущность "напоминание" и присваиваю ей значения, которые получил в сообщении пользователя
                    NotificationTask notificationTask = new NotificationTask();
                    notificationTask.setChatId(chatId);
                    String notification = matcher.group(3);
                    notificationTask.setNotification(notification);
                    LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), dateTimeFormatter);
                    notificationTask.setDateTime(dateTime);

                    // сохраняю напоминание в БД
                    notificationTaskRepository.save(notificationTask);
                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    Long chatId = update.message().chat().id();
                    SendMessage message = new SendMessage(chatId, "Извини, я тебя не понимаю, отправь сообщение вида *ДД.ММ.ГГГГ ЧЧ:ММ НАПОМИНАНИЕ*");
                    SendResponse response = telegramBot.execute(message);
                }
            });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
