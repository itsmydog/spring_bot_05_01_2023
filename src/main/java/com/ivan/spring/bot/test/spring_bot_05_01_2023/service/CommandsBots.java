package com.ivan.spring.bot.test.spring_bot_05_01_2023.service;

import com.ivan.spring.bot.test.spring_bot_05_01_2023.config.BotConfig;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.DepartamentsRepository;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.Departments;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.User;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class CommandsBots extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartamentsRepository departamentRepository;

    final BotConfig botConfig;
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String CREATE_USER = "Создать пользователя";
    static final String DELETE_USER = "Удалить пользователя";
    static final String READ_USER = "Показать всех пользователей";
    static final String UPDATE_USER = "Обновить пользователя";
    static final String CREATE_DEPARTMENTS = "Создать департамент";
    static final String READ_DEPARTMENTS = "Посмотреть департаменты";
    static final String DELETE_DEPARTMENTS = "Удалить департамент";
    static final String UPDATE_DEPARTMENTS = "Обновить департамент";


    enum State {
        Start,
        Name,
        Country,
        ID_NEW,
        Name_New,
        Country_NEW,
        Start_Delete,
        Name_Departments,
        Max_Salary,
        Min_Salary,
        Departments_Id,
        Departments_Delete,
        Departments_Update,
        Departments_New_Name,
        Departments_New_MinSalary,
        Departments_New_MaxSalary

    }

    public State state = State.Start;
    public User user = new User();
    public Departments dep = new Departments();


    public CommandsBots(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "запустить бота"));
        botCommandList.add(new BotCommand(CREATE_USER, "Записать"));
        botCommandList.add(new BotCommand(READ_USER, "показать базу данных сотрудников"));
        botCommandList.add(new BotCommand(DELETE_USER, "удалить сотрудника"));
        botCommandList.add(new BotCommand(UPDATE_USER, "редактировать данные сотрудника"));
        botCommandList.add(new BotCommand(CREATE_DEPARTMENTS, "создать отдел"));
        botCommandList.add(new BotCommand(READ_DEPARTMENTS, "посмотреть отдел"));
        botCommandList.add(new BotCommand(DELETE_DEPARTMENTS, "удалить отдел"));
        botCommandList.add(new BotCommand(UPDATE_DEPARTMENTS, "обновить отдел"));
//        botCommandList.add(new BotCommand(ShowAllUsers, "delete my data"));

        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {

        }
    }


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                startCommand(chatId, update.getMessage().getChat().getFirstName());
                //CRUD departments
            } else if (messageText.equals(CREATE_DEPARTMENTS)) {
                sendMessage(chatId, "введите название раздела");
                state = State.Name_Departments;
            } else if (messageText.equals(READ_DEPARTMENTS)) {
                var departaments = departamentRepository.findAll();
                for (Departments depOne : departaments) {
                    int id = depOne.getId();
                    String name = depOne.getDepartmentName();
                    long minSalary = depOne.getMinSalary();
                    long maxSalary = depOne.getMaxSalary();
                    sendMessage(chatId, "id: " + id + " отдел " + name + " от " +
                            minSalary + " до " + maxSalary);
                }
                state = State.Start;

            } else if (messageText.equals(DELETE_DEPARTMENTS)) {
                sendMessage(chatId, "введите Id для удаления ОТДЕЛА");
                state = State.Departments_Delete;
            } else if (messageText.equals(UPDATE_DEPARTMENTS)) {
                sendMessage(chatId, "введите Id для обновления ОТДЕЛА");
                state = State.Departments_Update;



            //CRUD user
            } else if (messageText.equals(READ_USER)) {
                var users = userRepository.findAll();
                for (User userOne : users) {
                    int id = (int) userOne.getId();
                    String name = userOne.getName();
                    String country = userOne.getCountry();
                    Departments idDep = userOne.getDepartments();
                    sendMessage(chatId, "id: " + id + " Имя: " + name + ", страна: " + country + " отдел "
                            + idDep.getDepartmentName());
                }
            } else if (messageText.equals(UPDATE_USER)) {
                sendMessage(chatId, "Введите Id для изменения РАБОТНИКА");
                state = State.ID_NEW;

            } else if (messageText.equals(DELETE_USER)) {
                sendMessage(chatId, "введите Id для удаления РАБОТНИКА");
                state = State.Start_Delete;

            } else if (messageText.equals(CREATE_USER)) {
                sendMessage(chatId, "введите имя");
                state = State.Name;
            } else {
                switch (state) {
                    //Create User
                    case Name:
                        user.setName(messageText);
                        sendMessage(chatId, "введите свой город");
                        state = State.Country;
                        break;
                    case Country:
                        user.setCountry(messageText);
                        sendMessage(chatId, "введите Id раздела, в котором будет работать пользователь");
                        state = State.Departments_Id;
                        break;
                    case Departments_Id:
                        Long departmentsId = Long.parseLong(messageText);
                        Departments department = departamentRepository.findById(departmentsId).orElse(null);
                        if (department != null) {
                            sendMessage(chatId, "Работник по имени " + user.getName() + ", из: " + user.getCountry() +
                                    " назначен в отдел " + department.getDepartmentName());
                            department.addUserToDepartments(user);
                            departamentRepository.save(department);
                            state = State.Start;
                        } else {
                            sendMessage(chatId, "нет такого отдела");
                        }
                        break;


                    //Delete User
                    case Start_Delete:
                        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()) {
                            user.setId(Integer.parseInt(messageText));
                            sendMessage(chatId, "Сотрудник удален");
                            state = State.Start;
                            userRepository.deleteById(user.getId());
                            sendMessage(chatId, "Вот список оставшихся");
                            var users = userRepository.findAll();
                            for (User userOne : users) {
                                int id = (int) userOne.getId();
                                String name = userOne.getName();
                                String country = userOne.getCountry();
                                sendMessage(chatId, "id: " + id + " Имя: " + name + ", страна: " + country);
                            }
                        } else {
                            sendMessage(chatId, "нет такого пользователя, попробуйте еще раз");
                        }
                        break;
                    // Update user
                    case ID_NEW:
                        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()) {
                            user.setId(Integer.parseInt(messageText));
                            sendMessage(chatId, "Введите новое имя");
                            state = State.Name_New;
                        } else {
                            sendMessage(chatId, "нет такого пользователя, попробуйте еще раз");
                        }
                        break;
                    case Name_New:
                        user.setName(messageText);
                        sendMessage(chatId, "Введите город");
                        state = State.Country_NEW;
                        break;
                    case Country_NEW:
                        user.setCountry(messageText);
                        userRepository.save(user);
                        sendMessage(chatId, "Данные изменены");
                        state = State.Start;
                        break;


                    //Create Department
                    case Name_Departments:
                        dep.setDepartmentName(messageText);
                        sendMessage(chatId, "введите максимальный размер оплаты труда");
                        state = State.Max_Salary;
                        break;
                    case Max_Salary:
                        dep.setMaxSalary(Integer.parseInt(messageText));
                        sendMessage(chatId, "введите минимальный размер оплаты труда");
                        state = State.Min_Salary;
                        break;
                    case Min_Salary:
                        dep.setMinSalary(Integer.parseInt(messageText));
                        sendMessage(chatId, "отдел " + dep.getDepartmentName() + " с заработной платой от " +
                                dep.getMinSalary() + " и до " + dep.getMaxSalary() + " добавлен");
                        departamentRepository.save(dep);
                        state = State.Start;
                        break;
                        //  Delete department
                    case Departments_Delete:
                        if (departamentRepository.existsById(Long.parseLong(messageText))) {
                            dep.setId(Integer.parseInt(messageText));
                            sendMessage(chatId, "Отдел: " + dep.getDepartmentName() + " удален");
                            departamentRepository.delete(dep);
                            var departaments = departamentRepository.findAll();
                            sendMessage(chatId, "Список отделов");
                            for (Departments depOne : departaments) {
                                int id = depOne.getId();
                                String name = depOne.getDepartmentName();
                                long minSalary = depOne.getMinSalary();
                                long maxSalary = depOne.getMaxSalary();
                                sendMessage(chatId, "id: " + id + " отдел " + name + " от " +
                                        minSalary + " до " + maxSalary);
                            }
                            state = State.Start;
                        } else {
                            sendMessage(chatId, "Нет такого отдела, попробуйте еще раз.\nВот список отделов");
                            var departaments = departamentRepository.findAll();
                            for (Departments depOne : departaments) {
                                int id = depOne.getId();
                                String name = depOne.getDepartmentName();
                                long minSalary = depOne.getMinSalary();
                                long maxSalary = depOne.getMaxSalary();
                                sendMessage(chatId, "id: " + id + " отдел " + name + " от " +
                                        minSalary + " до " + maxSalary);
                            }
                        }
                        break;

                    case Departments_Update:
                        if (departamentRepository.existsById(Long.parseLong(messageText))){
                            dep.setId(Integer.parseInt(messageText));
                            sendMessage(chatId, "Введите новое название отдела");
                            state = State.Departments_New_Name;
                        } else {
                            sendMessage(chatId, "Нет такого отдела");
                        }
                        break;
                    case Departments_New_Name:
                        dep.setDepartmentName(messageText);
                        sendMessage(chatId, "введите максимальный размер оплаты труда");
                        state = State.Departments_New_MaxSalary;
                        break;
                    case Departments_New_MaxSalary:
                        dep.setMaxSalary(Integer.parseInt(messageText));
                        sendMessage(chatId, "введите минимальный размер оплаты труда");
                        state = State.Departments_New_MinSalary;
                        break;
                    case Departments_New_MinSalary:
                        dep.setMinSalary(Integer.parseInt(messageText));
                        sendMessage(chatId,  "отдел " + dep.getDepartmentName() + " с заработной платой от " +
                                dep.getMinSalary() + " и до " + dep.getMaxSalary() + " обновлен");
                        departamentRepository.save(dep);
                        state = State.Start;
                        break;


                    default:
                        sendMessage(chatId, "Нет такой команды");
                }
            }

        }

//        } else if (update.hasCallbackQuery()) {
//            Message msg = update.getCallbackQuery().getMessage();
//            String callBackData = update.getCallbackQuery().getData();
//            long messageId = update.getCallbackQuery().getMessage().getMessageId();
//            long chatId = update.getCallbackQuery().getMessage().getChatId();
//
//            if (callBackData.equals(YES_BUTTON)) {
//                if (msg != null) {
//                }
//                String text = "You pressed YES button";
//                executeEditMessageText(text, chatId, messageId);
//
//            } else if (callBackData.equals(NO_BUTTON)) {
//                String text = "You pressed NO button";
//                executeEditMessageText(text, chatId, messageId);
//            }
//        }


    }


    private void startCommand(long chatId, String firstName) {
        String answer = "Привет " + firstName + " этот бот умеет регистрировать пользователей в какой то базе";
        sendMessage(chatId, answer);
    }

    public void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        //Клавиатура снизу
//        InlineKeyboardButton buttonOne = new InlineKeyboardButton();
//        KeyboardButton button = new KeyboardButton();
//        button.setText("Создать юзера");


        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow rowOneLine = new KeyboardRow();


        rowOneLine.add(CREATE_USER);
        rowOneLine.add(READ_USER);
        keyboardRows.add(rowOneLine);

        rowOneLine = new KeyboardRow();
        rowOneLine.add(DELETE_USER);
        rowOneLine.add(UPDATE_USER);
        keyboardRows.add(rowOneLine);

        rowOneLine = new KeyboardRow();
        rowOneLine.add(CREATE_DEPARTMENTS);
        rowOneLine.add(READ_DEPARTMENTS);
        rowOneLine.add(DELETE_DEPARTMENTS);
        rowOneLine.add(UPDATE_DEPARTMENTS);
        keyboardRows.add(rowOneLine);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButon = new InlineKeyboardButton();
        yesButon.setText("Yes");
        yesButon.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInline.add(yesButon);
        rowInline.add(noButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }
}
