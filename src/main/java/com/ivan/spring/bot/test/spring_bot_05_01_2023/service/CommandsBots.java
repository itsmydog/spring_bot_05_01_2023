package com.ivan.spring.bot.test.spring_bot_05_01_2023.service;

import com.ivan.spring.bot.test.spring_bot_05_01_2023.config.BotConfig;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.DepartmentsCommands;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.State;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.StateSingleton;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.UserCommands;
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
import static com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.BotCommands.*;
import static com.ivan.spring.bot.test.spring_bot_05_01_2023.handler.State.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CommandsBots extends TelegramLongPollingBot {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public DepartamentsRepository departamentRepository;
    @Autowired
    UserCommands userCommands;
    @Autowired
    DepartmentsCommands departmentsCommands;
    @Autowired
    User user;
    @Autowired
    Departments dep;
    private StateSingleton stateSingleton = StateSingleton.getInstance();


    final BotConfig botConfig;






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
            } else if (messageText.equals("/help")) {
                helpCommand(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals(USER)) {
                userButton(chatId);

            } else if (messageText.equals(DEPARTMENTS)) {
                departmentButton(chatId);
            } else {
                switch (stateSingleton.getState()) {
                    case One_Employee:
                        userCommands.userFind(chatId, messageText);
                        break;
                    case Name:
                        userCommands.name(chatId, messageText);
                        break;
                    case SurName:
                        userCommands.surName(chatId, messageText);
                        break;
                    case Departments_Id:
                        userCommands.departmentId(chatId, messageText);
                        break;

                    //Delete User
                    case Start_Delete:
                        userCommands.userDelete(chatId, messageText);
                        break;

                    // Update user
                    case ID_NEW:
                        userCommands.idNew(chatId, messageText);
                        break;
                    case Name_New:
                        userCommands.nameNew(chatId, messageText);
                        break;
                    case SurName_NEW:
                       userCommands.surNameNew(chatId, messageText);
                        break;
                    case Departments_New:
                        userCommands.departmentNew(chatId, messageText);
                        break;

                    //Create Department
                    case Name_Departments:
                        departmentsCommands.nameDepatments(chatId, messageText);
                        break;
                    case Max_Salary:
                        departmentsCommands.maxSalary(chatId, messageText);
                        break;
                    case Min_Salary:
                        departmentsCommands.minSalary(chatId, messageText);
                        break;

                    //  Delete department
                    case Departments_Delete:
                        departmentsCommands.departmentDelete(chatId, messageText);
                        break;

                    // Update department
                    case Departments_Update:
                        departmentsCommands.departmentUpdate(chatId, messageText);
                        break;
                    case Departments_New_Name:
                        departmentsCommands.departmentNewName(chatId, messageText);
                        break;
                    case Departments_New_MaxSalary:
                        departmentsCommands.departmentNewMaxSalary(chatId, messageText);
                        break;
                    case Departments_New_MinSalary:
                        departmentsCommands.departmentNewMinSalary(chatId, messageText);
                        break;

                    default:
                        sendMessage(chatId, "Нет такой команды");
                }
            }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(USER_NEW_BUTTON) && stateSingleton.getState() == State.ID_NEW) {
                String text = "Введите новое имя";
                sendMessage(chatId, text);
                stateSingleton.setState(State.Name_New);

            } else if (callBackData.equals(DEPARTMENT_NEW_BUTTON) && stateSingleton.getState() == State.ID_NEW) {
                String text = "Введите Id отдела";
                sendMessage(chatId, text);
                stateSingleton.setState(State.Departments_New);
            } else if (callBackData.equals(USER_CREATE_BUTTON)) {
                sendMessage(chatId, "Введите имя с большой буквы и максимум 30 символов (без цифр)");
                stateSingleton.setState(Name);
            } else if (callBackData.equals(USER_READ_BUTTON)) {
               userCommands.readUser(chatId);
//                userCommands.TestButton(chatId);
            } else if (callBackData.equals(USER_DELETE_BUTTON)) {
                sendMessage(chatId, "Введите Id для удаления РАБОТНИКА");
                stateSingleton.setState(State.Start_Delete);
            } else if (callBackData.equals(USER_UPDATE_BUTTON)) {
                sendMessage(chatId, "Введите Id работника для его изменений");
                stateSingleton.setState(State.ID_NEW);
            } else if (callBackData.equals(USER_FIND_BUTTON)) {
                sendMessage(chatId, "Введите Имя или Фамилию сотрудника");
                stateSingleton.setState(One_Employee);
            } else if (callBackData.equals(DEPARTMENT_CREATE_BUTTON)) {
                sendMessage(chatId, "Введите название ОТДЕЛА");
                stateSingleton.setState(State.Name_Departments);
            } else if (callBackData.equals(DEPARTMENT_READ_BUTTON)) {
                departmentsCommands.readDepartments(chatId);
            } else if (callBackData.equals(DEPARTMENT_DELETE_BUTTON)) {
//                sendMessage(chatId, "Введите Id для удаления ОТДЕЛА");
//                state = State.Departments_Delete;
                sendMessage(chatId, "Функция в разработке");
                stateSingleton.setState(State.Start);
            } else if (callBackData.equals(DEPARTMENT_UPDATE_BUTTON)) {
                sendMessage(chatId, "Введите Id для обновления ОТДЕЛА");
                stateSingleton.setState(State.Departments_Update);

            } else {
                sendMessage(chatId, "Вам нужно выбрать команду");
                stateSingleton.setState(State.Start);
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

    public void TestButton (long chatId){
        sendMessage(chatId, "Test");
    }


    private void startCommand(long chatId, String firstName) {
        String answer = "Привет " + firstName + " этот бот умеет регистрировать пользователей в какой то базе";
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

    public void userButton(long chatId) {
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

    public void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }


}
