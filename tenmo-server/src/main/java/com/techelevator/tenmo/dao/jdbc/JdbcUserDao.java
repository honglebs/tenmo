package com.techelevator.tenmo.dao.jdbc;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.models.Avatar;
import com.techelevator.tenmo.models.Color;
import com.techelevator.tenmo.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class JdbcUserDao implements UserDao
{

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal STARTING_BALANCE_FOR_GREGOR = new BigDecimal("10000.00"); // Gregor gets a blessed account with 10x the starting TE bucks!
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getIdByUsername(String username)
    {
        if (username == null)
        {
            throw new IllegalArgumentException("Username cannot be null");
        }

        int userId;
        try
        {
            userId = jdbcTemplate.queryForObject("SELECT user_id FROM tenmo_user WHERE username = ?", int.class, username);
        }
        catch (NullPointerException | EmptyResultDataAccessException e)
        {
            throw new UsernameNotFoundException("User " + username + " was not found.");
        }

        return userId;
    }

    @Override
    public User getUserById(int userId)
    {
        String sql = "SELECT tu.user_id " +
                " , tu.username " +
                " , tu.password_hash " +
                " , a.avatar_id " +
                " , a.avatar_desc " +
                " , a.avatar_line_1 " +
                " , a.avatar_line_2 " +
                " , a.avatar_line_3 " +
                " , a.avatar_line_4 " +
                " , a.avatar_line_5 " +
                " , ac.avatar_color_id " +
                " , ac.avatar_color_desc " +
                "FROM tenmo_user as tu " +
                "JOIN avatar as a " +
                "ON tu.avatar_id = a.avatar_id " +
                "JOIN avatar_color as ac " +
                "ON tu.avatar_color_id = ac.avatar_color_id " +
                "WHERE tu.user_id = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next())
        {
            return mapRowToUser(results);
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<User> getAll()
    {
        List<User> users = new ArrayList<>();
        String sql = "SELECT tu.user_id " +
                ", tu.username " +
                ", tu.password_hash " +
                ", a.avatar_id " +
                ", a.avatar_desc " +
                ", a.avatar_line_1 " +
                ", a.avatar_line_2 " +
                ", a.avatar_line_3 " +
                ", a.avatar_line_4 " +
                ", a.avatar_line_5 " +
                ", ac.avatar_color_id " +
                ", ac.avatar_color_desc " +
                "FROM tenmo_user as tu " +
                "JOIN avatar as a " +
                "ON tu.avatar_id = a.avatar_id " +
                "JOIN avatar_color as ac " +
                "ON tu.avatar_color_id = ac.avatar_color_id " +
                "ORDER BY tu.user_id;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while (results.next())
        {
            User user = mapRowToUser(results);
            users.add(user);
        }

        return users;
    }

    @Override
    public List<User> getAllExceptCurrent(int id) {
        List<User> users = getAll();
        List<User> usersExceptCurrent = users.stream()
                                             .filter(u -> u.getId() != id)
                                             .collect(Collectors.toList());

        return usersExceptCurrent;
    }

    @Override
    public User getByUsername(String username)
    {
        if (username == null)
        {
            throw new IllegalArgumentException("Username cannot be null");
        }

        String sql = "SELECT tu.user_id " +
                " , tu.username " +
                " , tu.password_hash " +
                " , a.avatar_id " +
                " , a.avatar_desc " +
                " , a.avatar_line_1 " +
                " , a.avatar_line_2 " +
                " , a.avatar_line_3 " +
                " , a.avatar_line_4 " +
                " , a.avatar_line_5 " +
                " , ac.avatar_color_id " +
                " , ac.avatar_color_desc " +
                "FROM tenmo_user as tu " +
                "JOIN avatar as a " +
                "ON tu.avatar_id = a.avatar_id " +
                "JOIN avatar_color as ac " +
                "ON tu.avatar_color_id = ac.avatar_color_id " +
                "WHERE tu.username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next())
        {
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password)
    {
        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash, avatar_id, avatar_color_id) VALUES (?, ?, ?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        int avatarId = getAvatarId(username);
        int colorId = getColorId();
        Integer newUserId;
        newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash, avatarId, colorId);

        if (newUserId == null)
        {
            return false;
        }

        // create account
        sql = "INSERT INTO account (user_id, balance) values(?, ?)";
        try
        {
            BigDecimal startBalance = username.equalsIgnoreCase("gregor")
                                    ? STARTING_BALANCE_FOR_GREGOR // Gregor gets a blessed account with 10x the starting TE bucks!
                                    : STARTING_BALANCE;
            jdbcTemplate.update(sql, newUserId, startBalance);
        }
        catch (DataAccessException e)
        {
            return false;
        }

        return true;
    }

    // helper function to map each row returned from the sql query to a user object
    private User mapRowToUser(SqlRowSet rs)
    {
        User user = new User();

        // set id, username, password, etc.
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");

        Avatar avatar = new Avatar();
        Color color = new Color();

        // set avatar
        avatar.setAvatarId(rs.getInt("avatar_id"));
        avatar.setAvatarDesc(rs.getString("avatar_desc"));
        avatar.setAvatarLine1(rs.getString("avatar_line_1"));
        avatar.setAvatarLine2(rs.getString("avatar_line_2"));
        avatar.setAvatarLine3(rs.getString("avatar_line_3"));
        avatar.setAvatarLine4(rs.getString("avatar_line_4"));
        avatar.setAvatarLine5(rs.getString("avatar_line_5"));

        // Set color
        color.setColorId(rs.getInt("avatar_color_id"));
        color.setColorDesc(rs.getString("avatar_color_desc"));

        avatar.setColor(color);

        user.setAvatar(avatar);

        return user;
    }

    // helper function to determine the default avatarId to be linked to the user's account when they register
    // By default, the avatar will be the first character of username if it starts w/ a-z; otherwise, a robot
    // Avatars can be changed on client side; this just sets the default when an account is first registered
    private int getAvatarId(String username) {
        char c = username.charAt(0);

        switch(c) {
            case 'a':
                return 7;
            case 'b':
                return 8;
            case 'c':
                return 9;
            case 'd':
                return 10;
            case 'e':
                return 11;
            case 'f':
                return 12;
            case 'g':
                return 13;
            case 'h':
                return 14;
            case 'i':
                return 15;
            case 'j':
                return 16;
            case 'k':
                return 17;
            case 'l':
                return 18;
            case 'm':
                return 19;
            case 'n':
                return 20;
            case 'o':
                return 21;
            case 'p':
                return 22;
            case 'q':
                return 23;
            case 'r':
                return 24;
            case 's':
                return 25;
            case 't':
                return 26;
            case 'u':
                return 27;
            case 'v':
                return 28;
            case 'w':
                return 29;
            case 'x':
                return 30;
            case 'y':
                return 31;
            case 'z':
                return 32;
            default:
                return 6;
        }

    }

    // helper function to determine the default colorId to be linked to the user's avatar when they register
    // By default, the color will be randomly generated. Colors can be changed on client side;
    // this just sets the default when an account is first registered
    private int getColorId() {
        // generate a random number between 1-7
        int min = 1;
        int max = 7;
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;

        }

}
