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

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                // Process your updates here
                // проверяю, приходит ли команда старт
                if (update.message().text().equals("/start")) {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    // запоминаю айди чата
                    Long chatId = update.message().chat().id();
                    // создаю сообщение
                    SendMessage message = new SendMessage(chatId, "Здарова");
                    // отправляю приветствие
                    SendResponse response = telegramBot.execute(message);
                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    // запоминаю айди чата
                    Long chatId = update.message().chat().id();
                    // создаю сообщение
                    SendMessage message = new SendMessage(chatId, "моя твоя не понимать");
                    // отправляю сообщение
                    SendResponse response = telegramBot.execute(message);
                }
            });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
//     TODO добавить по заданию ликвибейс + обработку сообщений (брать дату-время и напоминание, записывать в МАПУ, где ключом будет дата, а значением сообщение)
//     TODO потом в случае когда таймер совпадает с датой, вытягиваем по дате сообщение и присылаем в нужный нам чат (поиск чата еще не продумал, возможно вторая мапа с датой и чатАйди)


}
