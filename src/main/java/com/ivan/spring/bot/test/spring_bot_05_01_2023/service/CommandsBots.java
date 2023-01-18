package com.ivan.spring.bot.test.spring_bot_05_01_2023.service;

import com.ivan.spring.bot.test.spring_bot_05_01_2023.config.BotConfig;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.State;
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

@Component
@Slf4j
public class CommandsBots extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartamentsRepository departamentRepository;

    final BotConfig botConfig;

    static final String DEPARTMENT_CREATE_BUTTON = "DEPARTMENT_CREATE_BUTTON";
    static final String DEPARTMENT_READ_BUTTON = "DEPARTMENT_READ_BUTTON";
    static final String DEPARTMENT_DELETE_BUTTON = "DEPARTMENT_DELETE_BUTTON";
    static final String DEPARTMENT_UPDATE_BUTTON = "DEPARTMENT_UPDATE_BUTTON";
    static final String USER_NEW_BUTTON = "USER_NEW_BUTTON";
    static final String USER_FIND_BUTTON = "USER_FIND_BUTTON";
    static final String USER_UPDATE_BUTTON = "USER_UPDATE_BUTTON";
    static final String USER_CREATE_BUTTON = "USER_CREATE_BUTTON";
    static final String USER_DELETE_BUTTON = "USER_DELETE_BUTTON";
    static final String USER_READ_BUTTON = "USER_READ_BUTTON";
    static final String DEPARTMENT_NEW_BUTTON = "DEPARTMENT_NEW_BUTTON";
    static final String USER = "Работа с пользователями";
    static final String DEPARTMENTS = "Работа с отделами";


    State state;
    public User user = new User();
    public Departments dep = new Departments();


    public CommandsBots(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "запустить бота"));
        botCommandList.add(new BotCommand("/help", "вызвать бесполезный текст"));
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
            } else if (messageText.equals("/help")){
                helpCommand(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals(USER)) {
                userButton(chatId);

            } else if (messageText.equals(DEPARTMENTS)) {
                departmentButton(chatId);


            } else {
                switch (state) {
                    case One_Employee:
                        userFind(chatId, messageText);
                        break;

                    //Create User
                    case Name:
                        name(chatId, messageText);
                        break;
                    case SurName:
                        surName(chatId, messageText);
                        break;
                    case Departments_Id:
                        departmentId(chatId, messageText);
                        break;

                    //Delete User
                    case Start_Delete:
                        userDelete(chatId, messageText);
                        break;

                    // Update user
                    case ID_NEW:
                        idNew(chatId, messageText);
                        break;
                    case Name_New:
                        nameNew(chatId, messageText);
                        break;
                    case SurName_NEW:
                        surNameNew(chatId, messageText);
                        break;
                    case Departments_New:
                        departmentNew(chatId, messageText);
                        break;

                    //Create Department
                    case Name_Departments:
                        nameDepatments(chatId, messageText);
                        break;
                    case Max_Salary:
                        maxSalary(chatId, messageText);
                        break;
                    case Min_Salary:
                        minSalary(chatId, messageText);
                        break;

                    //  Delete department
                    case Departments_Delete:
                        departmentDelete(chatId, messageText);
                        break;

                    // Update department
                    case Departments_Update:
                        departmentUpdate(chatId, messageText);
                        break;
                    case Departments_New_Name:
                        departmentNewName(chatId, messageText);
                        break;
                    case Departments_New_MaxSalary:
                        departmentNewMaxSalary(chatId, messageText);
                        break;
                    case Departments_New_MinSalary:
                        departmentNewMinSalary(chatId, messageText);
                        break;

                    default:
                        sendMessage(chatId, "Нет такой команды");
                }
            }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(USER_NEW_BUTTON) && state == State.ID_NEW) {
                String text = "Введите новое имя";
                sendMessage(chatId, text);
                state = State.Name_New;

            } else if (callBackData.equals(DEPARTMENT_NEW_BUTTON) && state == State.ID_NEW) {
                String text = "Введите Id отдела";
                sendMessage(chatId, text);
                state = State.Departments_New;
            } else if (callBackData.equals(USER_CREATE_BUTTON)) {
                sendMessage(chatId, "введите имя");
                state = State.Name;
            } else if (callBackData.equals(USER_READ_BUTTON)) {
                readUser(chatId);
            } else if (callBackData.equals(USER_DELETE_BUTTON)) {
                sendMessage(chatId, "введите Id для удаления РАБОТНИКА");
                state = State.Start_Delete;
            } else if (callBackData.equals(USER_UPDATE_BUTTON)) {
                sendMessage(chatId, "введите Id работника для его изменений");
                state = State.ID_NEW;
            } else if (callBackData.equals(USER_FIND_BUTTON)) {
                sendMessage(chatId, "введите Имя или Фамилию сотрудника");
                state = State.One_Employee;
            } else if (callBackData.equals(DEPARTMENT_CREATE_BUTTON)) {
                sendMessage(chatId, "введите название ОТДЕЛА");
                state = State.Name_Departments;
            } else if (callBackData.equals(DEPARTMENT_READ_BUTTON)) {
                readDepartments(chatId);
            } else if (callBackData.equals(DEPARTMENT_DELETE_BUTTON)) {
                sendMessage(chatId, "введите Id для удаления ОТДЕЛА");
                state = State.Departments_Delete;
            } else if (callBackData.equals(DEPARTMENT_UPDATE_BUTTON)) {
                sendMessage(chatId, "введите Id для обновления ОТДЕЛА");
                state = State.Departments_Update;


            } else {
                sendMessage(chatId, "Вам нужно выбрать команду");
                state = State.Start;
            }
        }


    }

    private void helpCommand(long chatId, String firstName) {
        String answer = "Этот бот делает CRUD функционал.\n" +
                "кофе и чай пока еще не делает\n" +
                "да и функционал тут так себе\n" +
                "но я рад что ты зашел кусок мяса\n" +
                "с ником " + firstName;
        sendMessage(chatId, answer);
    }

    private void departmentButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие с пользователем");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var createButton = new InlineKeyboardButton();
        createButton.setText("Создать отдел");
        createButton.setCallbackData(DEPARTMENT_CREATE_BUTTON);
        var readButton = new InlineKeyboardButton();
        readButton.setText("Посмотреть все отделы");
        readButton.setCallbackData(DEPARTMENT_READ_BUTTON);

        rowInline.add(createButton);
        rowInline.add(readButton);

        rowsInline.add(rowInline);
        List<InlineKeyboardButton> rowInlineTwo = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Удалить отдел");
        deleteButton.setCallbackData(DEPARTMENT_DELETE_BUTTON);
        var updateButton = new InlineKeyboardButton();
        updateButton.setText("Обновить отдел");
        updateButton.setCallbackData(DEPARTMENT_UPDATE_BUTTON);

        rowInlineTwo.add(deleteButton);
        rowInlineTwo.add(updateButton);

        rowsInline.add(rowInlineTwo);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }

    private void userButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие с пользователем");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var createButton = new InlineKeyboardButton();
        createButton.setText("Создать работника");
        createButton.setCallbackData(USER_CREATE_BUTTON);
        var readButton = new InlineKeyboardButton();
        readButton.setText("Посмотреть всех работников");
        readButton.setCallbackData(USER_READ_BUTTON);

        rowInline.add(createButton);
        rowInline.add(readButton);

        rowsInline.add(rowInline);
        List<InlineKeyboardButton> rowInlineTwo = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Удалить работника");
        deleteButton.setCallbackData(USER_DELETE_BUTTON);
        var updateUser = new InlineKeyboardButton();
        updateUser.setText("Обновить работника");
        updateUser.setCallbackData(USER_UPDATE_BUTTON);

        rowInlineTwo.add(deleteButton);
        rowInlineTwo.add(updateUser);

        rowsInline.add(rowInlineTwo);

        List<InlineKeyboardButton> rowInlineThree = new ArrayList<>();
        var findUserButton = new InlineKeyboardButton();
        findUserButton.setText("Найти работников");
        findUserButton.setCallbackData(USER_FIND_BUTTON);

        rowInlineThree.add(findUserButton);

        rowsInline.add(rowInlineThree);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }

    private void departmentId(long chatId, String messageText) {
        Long departmentsId = Long.parseLong(messageText);
        Departments department = departamentRepository.findById(departmentsId).orElse(null);
        if (department != null) {
            sendMessage(chatId, "Работник " + user.getName() + " " + user.getSurName() +
                    " назначен в отдел " + department.getDepartmentName());
            department.addUserToDepartments(user);
            departamentRepository.save(department);
            state = State.Start;
        } else {
            sendMessage(chatId, "нет такого отдела");
        }
    }

    private void surName(long chatId, String messageText) {
        user.setSurName(messageText);
        sendMessage(chatId, "введите Id раздела, в котором будет работать пользователь");
        sendMessage(chatId, "вот список существующих отделов");
        var departaments = departamentRepository.findAll();
        for (Departments depOne : departaments) {
            int id = depOne.getId();
            String name = depOne.getDepartmentName();
            sendMessage(chatId, "id: " + id + " отдел " + name);
        }
        state = State.Departments_Id;
    }

    private void name(long chatId, String messageText) {
        user.setId(0);
        user.setName(messageText);
        sendMessage(chatId, "введите фамилию");
        state = State.SurName;
    }

    private void departmentNewMinSalary(long chatId, String messageText) {
        dep.setMinSalary(Integer.parseInt(messageText));
        sendMessage(chatId, "отдел " + dep.getDepartmentName() + " с заработной платой от " +
                dep.getMinSalary() + " и до " + dep.getMaxSalary() + " обновлен");
        departamentRepository.save(dep);
        state = State.Start;
    }

    private void departmentNewMaxSalary(long chatId, String messageText) {
        dep.setMaxSalary(Integer.parseInt(messageText));
        sendMessage(chatId, "введите минимальный размер оплаты труда");
        state = State.Departments_New_MinSalary;
    }

    private void departmentNewName(long chatId, String messageText) {
        dep.setDepartmentName(messageText);
        sendMessage(chatId, "введите максимальный размер оплаты труда");
        state = State.Departments_New_MaxSalary;
    }

    private void departmentUpdate(long chatId, String messageText) {
        if (departamentRepository.existsById(Long.parseLong(messageText))) {
            dep.setId(Integer.parseInt(messageText));
            sendMessage(chatId, "Введите новое название отдела");
            state = State.Departments_New_Name;
        } else {
            sendMessage(chatId, "Нет такого отдела");
        }
    }

    private void departmentDelete(long chatId, String messageText) {
        if (departamentRepository.existsById(Long.parseLong(messageText))) {
            dep.setId(Integer.parseInt(messageText));
            sendMessage(chatId, "Отдел: " + dep.getDepartmentName() + " удален");
            departamentRepository.delete(dep);
            var departaments = departamentRepository.findAll();
            sendMessage(chatId, "Список отделов");
            for (Departments depDel : departaments) {
                int id = depDel.getId();
                String name = depDel.getDepartmentName();
                sendMessage(chatId, "id: " + id + " отдел " + name);
            }
            state = State.Start;
        } else {
            sendMessage(chatId, "Нет такого отдела, попробуйте еще раз.\nВот список отделов");
            var departaments = departamentRepository.findAll();
            for (Departments depDel : departaments) {
                int id = depDel.getId();
                String name = depDel.getDepartmentName();
                sendMessage(chatId, "id: " + id + " отдел " + name);
            }
        }
    }

    private void minSalary(long chatId, String messageText) {
        dep.setMinSalary(Integer.parseInt(messageText));
        sendMessage(chatId, "отдел " + dep.getDepartmentName() + " с заработной платой от " +
                dep.getMinSalary() + " и до " + dep.getMaxSalary() + " добавлен");
        departamentRepository.save(dep);
        state = State.Start;
    }

    private void maxSalary(long chatId, String messageText) {
        dep.setMaxSalary(Integer.parseInt(messageText));
        sendMessage(chatId, "введите минимальный размер оплаты труда");
        state = State.Min_Salary;
    }

    private void nameDepatments(long chatId, String messageText) {
        dep.setId(0);
        dep.setDepartmentName(messageText);
        sendMessage(chatId, "введите максимальный размер оплаты труда");
        state = State.Max_Salary;
    }

    private void departmentNew(long chatId, String messageText) {
        Long departmentsIdNew = Long.parseLong(messageText);
        User user1 = userRepository.findById(user.getId()).orElse(null);
        Departments departmentNew = departamentRepository.findById(departmentsIdNew).orElse(null);
        if (departmentNew != null) {
            departmentNew.addUserToDepartments(user1);
            try {
                departamentRepository.save(departmentNew);
                sendMessage(chatId, "Работник " + user1.getName() + " " + user1.getSurName() +
                        " переведен в \nотдел " + departmentNew.getDepartmentName());
            } catch (Exception e) {
                sendMessage(chatId, "Работник " + user1.getName() + " " + user1.getSurName() +
                        " <<<НЕ ПЕРЕВЕДЕН>>>");
            } finally {
                state = State.Start;
            }
        } else {
            sendMessage(chatId, "нет такого отдела");
        }
    }

    private void nameNew(long chatId, String messageText) {
        user.setName(messageText);
        sendMessage(chatId, "Введите фамилию");
        state = State.SurName_NEW;
    }

    private void idNew(long chatId, String messageText) {
        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()) {
            user.setId(Integer.parseInt(messageText));
            UpdateUser(chatId);
        } else {
            sendMessage(chatId, "нет такого пользователя, попробуйте еще раз");
        }
    }

    private void surNameNew(long chatId, String messageText) {
        user.setSurName(messageText);
        User userNew = userRepository.findById(Long.valueOf(user.getId())).orElse(null);
        userRepository.save(user);
        Departments departmentNew2 = departamentRepository.findById(Long.valueOf(userNew.getDepartments().getId())).orElse(null);
        departmentNew2.addUserToDepartments(user);
        departamentRepository.save(departmentNew2);

        sendMessage(chatId, "Данные изменены");
        state = State.Start;
    }

    private void userDelete(long chatId, String messageText) {
        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()) {
            User userDel = userRepository.findById(Long.valueOf(messageText)).orElse(null);
            userDel.setDepartments(null);
            userRepository.save(userDel);
            user.setId(Integer.parseInt(messageText));
            sendMessage(chatId, "Сотрудник удален");
            state = State.Start;
            userRepository.deleteById(user.getId());
            sendMessage(chatId, "Вот список оставшихся");
            var users = userRepository.findAll();
            for (User userOne : users) {
                int id = (int) userOne.getId();
                String name = userOne.getName();
                String surName = userOne.getSurName();
                Departments idDep = userOne.getDepartments();
                sendMessage(chatId, "id: " + id + " " + name + " " + surName
                        + " Работает в " + idDep.getDepartmentName());
            }
        } else {
            sendMessage(chatId, "нет такого пользователя, попробуйте еще раз");
        }
    }

    private void userFind(long chatId, String messageText) {
        var usersFind = userRepository.findAll();
        int count = 0;
        for (User userOne : usersFind) {
            if (userOne.getName().equalsIgnoreCase(messageText)) {
                sendMessage(chatId, "Id " + userOne.getId() + " " + userOne.getName() + " " + userOne.getSurName() +
                        " из отдела " + userOne.getDepartments().getDepartmentName());
                count++;
            } else if (userOne.getSurName().equalsIgnoreCase(messageText)) {
                sendMessage(chatId, "Id " + userOne.getId() + " " + userOne.getName() + " " + userOne.getSurName() +
                        " из отдела " + userOne.getDepartments().getDepartmentName());
                count++;
            } else if (userOne.getDepartments().getDepartmentName().equalsIgnoreCase(messageText)) {
                sendMessage(chatId, "Id " + userOne.getId() + " " + userOne.getName() + " " + userOne.getSurName() +
                        " из отдела " + userOne.getDepartments().getDepartmentName());
                count++;
            }
        }
        if (count > 0) {
            sendMessage(chatId, "найдено " + count + " сотрудников");
            state = State.Start;
        } else {
            sendMessage(chatId, "у нас нет таких сотрудников");
        }
    }

    private void readDepartments(long chatId) {
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
    }

    private void readUser(long chatId) {
        var users = userRepository.findAll();
        for (User userOne : users) {
            int id = (int) userOne.getId();
            String name = userOne.getName();
            String surName = userOne.getSurName();
            sendMessage(chatId, "id: " + id + " Имя: " + name + ", Фамилия: " + surName + "\nотдел "
                    + userOne.getDepartments().getDepartmentName() + " ID: " + userOne.getDepartments().getId());
        }
    }


    private void startCommand(long chatId, String firstName) {
        String answer = "Привет " + firstName + " этот бот умеет регистрировать пользователей в какой то базе";
        sendMessage(chatId, answer);
    }

    public void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow rowOneLine = new KeyboardRow();

        rowOneLine.add(USER);
        keyboardRows.add(rowOneLine);

        rowOneLine = new KeyboardRow();
        rowOneLine.add(DEPARTMENTS);
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


    private void UpdateUser(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Что вы хотите изменить?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButon = new InlineKeyboardButton();
        yesButon.setText("Поменять данные работника");
        yesButon.setCallbackData(USER_NEW_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Перевод в другой отдел");
        noButton.setCallbackData(DEPARTMENT_NEW_BUTTON);

        rowInline.add(yesButon);
        rowInline.add(noButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }
}
