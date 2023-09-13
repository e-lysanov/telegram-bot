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

    private NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
            updates.forEach(update -> {
                Matcher matcher = pattern.matcher(update.message().text());
                logger.info("Processing update: {}", update);
                // Process your updates here
                // проверяю, приходит ли команда старт
                if (update.message().text().equals("/start")) {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    // запоминаю айди чата
                    Long chatId = update.message().chat().id();
                    // создаю сообщение
                    SendMessage message = new SendMessage(chatId, "Привет! Чтобы добавить напоминание, отправь сообщение вида *ДД.ММ.ГГГГ ЧЧ:ММ Напоминание*");
                    // отправляю приветствие
                    SendResponse response = telegramBot.execute(message);
                } else if (matcher.matches()) {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    // запоминаю айди чата
                    Long chatId = update.message().chat().id();
                    // создаю сообщение
                    SendMessage message = new SendMessage(chatId, "Понял-принял, все напомню!");
                    // отправляю сообщение
                    SendResponse response = telegramBot.execute(message);

                    NotificationTask notificationTask = new NotificationTask();
                    notificationTask.setChatId(chatId);
                    String notification = matcher.group(3);
                    notificationTask.setNotification(notification);
                    String date = matcher.group(1);
                    String time = matcher.group(2);
                    String dateTimeString = (date + time).substring(0, 16);
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    notificationTask.setDateTime(dateTime);
                    notificationTaskRepository.save(notificationTask);
                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    // запоминаю айди чата
                    Long chatId = update.message().chat().id();
                    // создаю сообщение
                    SendMessage message = new SendMessage(chatId, "Извини, я тебя не понимаю");
                    // отправляю сообщение
                    SendResponse response = telegramBot.execute(message);
                }
            });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
//     TODO добавить по заданию обработку сообщений (брать дату-время и напоминание, записывать в МАПУ, где ключом будет дата, а значением сообщение)
//     TODO потом в случае когда таймер совпадает с датой, вытягиваем по дате сообщение и присылаем в нужный нам чат (поиск чата еще не продумал, возможно вторая мапа с датой и чатАйди)


}
