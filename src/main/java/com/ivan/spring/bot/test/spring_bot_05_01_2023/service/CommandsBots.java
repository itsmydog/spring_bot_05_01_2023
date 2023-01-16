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

@Component
@Slf4j
public class CommandsBots extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartamentsRepository departamentRepository;

    final BotConfig botConfig;
    static final String USER_NEW_BUTTON = "USER_NEW_BUTTON";
    static final String DEPARTMENT_NEW_BUTTON = "DEPARTMENT_NEW_BUTTON";
    static final String CREATE_USER = "Создать пользователя";
    static final String DELETE_USER = "Удалить пользователя";
    static final String READ_USER = "Показать всех пользователей";
    static final String UPDATE_USER = "Обновить пользователя";
    static final String One_User = "поиск сотрудников по отделам";
    static final String CREATE_DEPARTMENTS = "Создать департамент";
    static final String READ_DEPARTMENTS = "Посмотреть департаменты";
    static final String DELETE_DEPARTMENTS = "Удалить департамент";
    static final String UPDATE_DEPARTMENTS = "Обновить департамент";


    enum State {
        Start,
        Name,
        SurName,
        ID_NEW,
        Name_New,
        SurName_NEW,
        Start_Delete,
        Name_Departments,
        Max_Salary,
        Min_Salary,
        Departments_Id,
        Departments_Delete,
        Departments_Update,
        Departments_New_Name,
        Departments_New_MinSalary,
        Departments_New,
        One_Employee, Departments_New_MaxSalary

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
        botCommandList.add(new BotCommand(One_User, "поиск сотрудников по отделам"));
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
            } else if (messageText.equals(One_User)) {
                sendMessage(chatId, "введите Имя или Фамилию сотрудника");
                state = State.One_Employee;
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
                    String surName = userOne.getSurName();
                    sendMessage(chatId, "id: " + id + " Имя: " + name + ", Фамилия: " + surName + "\nотдел "
                            + userOne.getDepartments().getDepartmentName() + " ID: " + userOne.getDepartments().getId());
                }
            } else if (messageText.equals(UPDATE_USER)) {
                sendMessage(chatId, "введите Id работника для его изменений");
                state = State.ID_NEW;

            } else if (messageText.equals(DELETE_USER)) {
                sendMessage(chatId, "введите Id для удаления РАБОТНИКА");
                state = State.Start_Delete;

            } else if (messageText.equals(CREATE_USER)) {
                sendMessage(chatId, "введите имя");
                state = State.Name;
            } else {
                switch (state) {
                    case One_Employee:
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
                        if (count > 0){
                            sendMessage(chatId, "найдено " + count + " сотрудников");
                            state = State.Start;
                        } else {
                            sendMessage(chatId, "у нас нет таких сотрудников");
                        }
                        break;
                    //Create User
                    case Name:
                        user.setId(0);
                        user.setName(messageText);
                        sendMessage(chatId, "введите фамилию");
                        state = State.SurName;
                        break;
                    case SurName:
                        user.setSurName(messageText);
                        sendMessage(chatId, "введите Id раздела, в котором будет работать пользователь");
                        state = State.Departments_Id;
                        break;
                    case Departments_Id:
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
                        break;


                    //Delete User
                    case Start_Delete:
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
                        break;
                    // Update user

                    case ID_NEW:
                        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()) {
                            user.setId(Integer.parseInt(messageText));
                            UpdateUser(chatId);
                        } else {
                            sendMessage(chatId, "нет такого пользователя, попробуйте еще раз");
                        }
                        break;
                    case Name_New:
                        user.setName(messageText);
                        sendMessage(chatId, "Введите фамилию");
                        state = State.SurName_NEW;
                        break;
                    case SurName_NEW:
                        user.setSurName(messageText);
                        User userNew = userRepository.findById(Long.valueOf(user.getId())).orElse(null);
                        userRepository.save(user);
                        Departments departmentNew2 = departamentRepository.findById(Long.valueOf(userNew.getDepartments().getId())).orElse(null);
                        departmentNew2.addUserToDepartments(user);
                        departamentRepository.save(departmentNew2);

                        sendMessage(chatId, "Данные изменены");
                        state = State.Start;
                        break;
                    case Departments_New:
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
                        break;


                    //Create Department
                    case Name_Departments:
                        dep.setId(0);
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
                            for (Departments depDel : departaments) {
                                int id = depDel.getId();
                                String name = depDel.getDepartmentName();
                                long minSalary = depDel.getMinSalary();
                                long maxSalary = depDel.getMaxSalary();
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
                        break;

                    case Departments_Update:
                        if (departamentRepository.existsById(Long.parseLong(messageText))) {
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
                        sendMessage(chatId, "отдел " + dep.getDepartmentName() + " с заработной платой от " +
                                dep.getMinSalary() + " и до " + dep.getMaxSalary() + " обновлен");
                        departamentRepository.save(dep);
                        state = State.Start;
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
            } else {
                sendMessage(chatId, "Вам нужно выбрать команду");
                state = State.Start;
            }
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
