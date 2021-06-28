package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.RequestDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.Request;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    private final ServerConfigurationService configurationService;
    private final PasswordEncoder passwordEncoder;
    private final RequestDao requestDao;
    private final UserDao userDao;
    private List<String> userRoles;


    @Autowired
    public UserServiceImpl(ServerConfigurationService configurationService, PasswordEncoder passwordEncoder, RequestDao requestDao, UserDao userDao) {
        this.configurationService = configurationService;
        this.passwordEncoder = passwordEncoder;
        this.requestDao = requestDao;
        this.userDao = userDao;
    }

    @PostConstruct
    void initialize() {
        this.userRoles = configurationService.getRoles();
    }

    @Override
    public List<String> getAvailableRoles() {
        return userRoles;
    }

    @Override
    public UUID createRequest(RequestForm requestForm) {
        UUID uuid = createUniqueUUID();
        Request request = new Request(uuid, new Date(), requestForm.getEmail());
        requestDao.save(request);
        return uuid;
    }

    /**
     * Creating unique {@link UUID}
     * @return unique {@link UUID}
     */
    private UUID createUniqueUUID() {
        UUID uuid = null;
        boolean isExists = true;
        while (isExists) {
            uuid = UUID.randomUUID();
            isExists = isRequestUUIDExists(uuid);
        }
        return uuid;
    }


    @Override
    public boolean isRequestUUIDExists(UUID uuid) {
        return requestDao.existsById(uuid);
    }


    @Override
    public List<Request> getInvitations() {
        return requestDao.findAll();
    }



    @Scheduled(cron = "0 0 0 * *")
    public void deleteOldRequest() {
        Calendar calendar = Calendar.getInstance();
        Date time0 = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date time1 = calendar.getTime();

        requestDao.deleteAllByCreationDateIsBetween(time0, time1);
    }


    @Override
    public void deleteRequestById(UUID uuid) {
        requestDao.deleteById(uuid);
    }


    @Override
    public void registerNewUserAccount(@NonNull UserDto userDto) {
        String role = configurationService.getDefaultRole();
        User user = new User(userDto.getUuid(), userDto.getLogin(), userDto.getEmail(), role,
                passwordEncoder.encode(userDto.getPassword()));
        userDao.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userDao.findAll();
    }

    @Override
    public void deleteUserById(UUID id) {
        userDao.deleteById(id);
    }

    @Override
    public User getUserById(UUID id) {
        return userDao.getOne(id);
    }

    @Override
    public void changeRole(ChangeRoleDto dto) {
        String id = dto.getId();
        User user = getUserById(UUID.fromString(id));
        user.setRole(dto.getRole());
        userDao.save(user);
    }

    @Override
    public void toggleEnabledStatus(String id, boolean status) {
        User user = getUserById(UUID.fromString(id));
        user.setEnabled(status);
        userDao.save(user);
    }
}
