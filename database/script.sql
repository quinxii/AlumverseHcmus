BEGIN;

CREATE DATABASE alumverse_hcmus;

BEGIN;

USE alumverse_hcmus;

DROP TABLE IF EXISTS role;

CREATE TABLE
    role (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        description VARCHAR(100),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS permission;

CREATE TABLE
    permission (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        description VARCHAR(100),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS role_permission;

CREATE TABLE
    role_permission (
        role_id TINYINT NOT NULL,
        permission_id TINYINT NOT NULL,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (role_id) REFERENCES role (id),
        FOREIGN KEY (permission_id) REFERENCES permission (id),
        PRIMARY KEY (role_id, permission_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS sex;

CREATE TABLE
    sex (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(50) NOT NULL UNIQUE,
        description TINYTEXT,
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS status_user_group;

CREATE TABLE
    status_user_group (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        description TINYTEXT,
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS status_post;

CREATE TABLE
    status_post (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        description TINYTEXT,
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS email_activation_code;

CREATE TABLE
    email_activation_code (
        email VARCHAR(255) NOT NULL UNIQUE,
        activation_code VARCHAR(8) NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        PRIMARY KEY (email (255))
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS faculty;

CREATE TABLE
    faculty (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        description TINYTEXT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag;

CREATE TABLE
    tag (
        id INT UNSIGNED NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS user;

CREATE TABLE
    user (
        id VARCHAR(36) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        pass VARCHAR(60) NOT NULL,
        full_name VARCHAR(100),
        phone VARCHAR(15),
        sex_id TINYINT,
        dob DATE,
        social_media_link TINYTEXT,
        faculty_id TINYINT,
        degree VARCHAR(50),
        about_me TEXT,
        avatar_url TINYTEXT,
        cover_url TINYTEXT,
        status_id TINYINT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        last_login DATETIME,
        online_status TINYINT (1) DEFAULT (0),
        email_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        phone_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        sex_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        dob_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        faculty_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        FOREIGN KEY (sex_id) REFERENCES sex (id),
        FOREIGN KEY (faculty_id) REFERENCES faculty (id),
        FOREIGN KEY (status_id) REFERENCES status_user_group (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS user_role;

CREATE TABLE
    user_role (
        user_id VARCHAR(36) NOT NULL,
        role_id TINYINT NOT NULL,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (role_id) REFERENCES role (id),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id, role_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS alumni;

CREATE TABLE
    alumni (
        user_id VARCHAR(36) NOT NULL,
        student_id VARCHAR(8),
        beginning_year SMALLINT,
        graduation_year SMALLINT,
        class VARCHAR(10),
        student_id_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        start_year_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        graduation_year_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        class_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS teacher;

CREATE TABLE
    teacher (
        user_id VARCHAR(36) NOT NULL,
        start_year SMALLINT,
        end_year SMALLINT,
        position VARCHAR(50),
        start_year_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        end_year_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        position_privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS job;

CREATE TABLE
    job (
        user_id VARCHAR(36) NOT NULL,
        company_name VARCHAR(255) NOT NULL,
        position VARCHAR(100) NOT NULL,
        start_time DATE,
        end_time DATE,
        privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id, company_name, position)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS education;

CREATE TABLE
    education (
        user_id VARCHAR(36) NOT NULL,
        school_name VARCHAR(255) NOT NULL,
        degree VARCHAR(50) NOT NULL,
        start_time DATE,
        end_time DATE,
        privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id, school_name, degree)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS achievement;

CREATE TABLE
    achievement (
        user_id VARCHAR(36) NOT NULL,
        name VARCHAR(255) NOT NULL,
        type VARCHAR(50) NOT NULL,
        time DATE,
        privacy ENUM ('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT ('PUBLIC'),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (user_id, name)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS verify_alumni;

CREATE TABLE
    verify_alumni (
        id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        student_id VARCHAR(8),
        beginning_year SMALLINT,
        social_media_link TINYTEXT,
        faculty_id TINYINT,
        comment TEXT,
        status ENUM ('PENDING', 'APPROVED', 'DENIED') DEFAULT ('PENDING'),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (faculty_id) REFERENCES faculty (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS follow_user;

CREATE TABLE
    follow_user (
        user_id VARCHAR(36) NOT NULL,
        follower_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (follower_id) REFERENCES user (id),
        PRIMARY KEY (user_id, follower_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS friend;

CREATE TABLE
    friend (
        user_id VARCHAR(36) NOT NULL,
        friend_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (friend_id) REFERENCES user (id),
        PRIMARY KEY (user_id, friend_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS request_friend;

CREATE TABLE
    request_friend (
        user_id VARCHAR(36) NOT NULL,
        friend_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (friend_id) REFERENCES user (id),
        PRIMARY KEY (user_id, friend_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS `group`;

CREATE TABLE
    `group` (
        id VARCHAR(36) NOT NULL,
        name VARCHAR(255) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        description TEXT,
        type VARCHAR(50),
        cover_url TINYTEXT,
        website TINYTEXT,
        privacy ENUM ('PUBLIC', 'PRIVATE') DEFAULT ('PUBLIC'),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        status_id TINYINT,
        participant_count INT DEFAULT (0),
        FOREIGN KEY (status_id) REFERENCES status_user_group (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS group_member;

CREATE TABLE
    group_member (
        group_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        role ENUM ('CREATOR', 'ADMIN', 'MEMBER') NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (group_id) REFERENCES `group` (id),
        PRIMARY KEY (group_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS request_join_group;

CREATE TABLE
    request_join_group (
        group_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (group_id) REFERENCES `group` (id),
        PRIMARY KEY (group_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS post_advise;

CREATE TABLE
    post_advise (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        title TINYTEXT,
        content TEXT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        published_at DATETIME,
        status_id TINYINT,
        children_comment_number INT DEFAULT (0),
        reaction_count INT DEFAULT (0),
        allow_multiple_votes TINYINT (1) DEFAULT (0),
        allow_add_options TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (status_id) REFERENCES status_post (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS picture_post_advise;

CREATE TABLE
    picture_post_advise (
        id VARCHAR(36) NOT NULL,
        post_advise_id VARCHAR(36) NOT NULL,
        picture_url VARCHAR(255) NOT NULL,
        picture_order TINYINT NOT NULL,
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        PRIMARY KEY (id, post_advise_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS post_group;

CREATE TABLE
    post_group (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        title TINYTEXT,
        content TEXT,
        group_id VARCHAR(36),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        published_at DATETIME,
        status_id TINYINT,
        children_comment_number INT DEFAULT (0),
        reaction_count INT DEFAULT (0),
        allow_multiple_votes TINYINT (1) DEFAULT (0),
        allow_add_options TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (status_id) REFERENCES status_post (id),
        FOREIGN KEY (group_id) REFERENCES `group` (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS picture_post_group;

CREATE TABLE
    picture_post_group (
        id VARCHAR(36) NOT NULL,
        post_group_id VARCHAR(36) NOT NULL,
        picture_url VARCHAR(255) NOT NULL,
        picture_order TINYINT NOT NULL,
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        PRIMARY KEY (id, post_group_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS event;

CREATE TABLE
    event (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        title TINYTEXT,
        content TEXT,
        thumbnail TINYTEXT,
        faculty_id TINYINT,
        organization_location TINYTEXT,
        organization_time DATETIME,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        published_at DATETIME,
        status_id TINYINT,
        views INT DEFAULT (0),
        participants INT DEFAULT (0),
        minimum_participants INT DEFAULT (0),
        maximum_participants INT DEFAULT (0),
        children_comment_number INT DEFAULT (0),
        FOREIGN KEY (status_id) REFERENCES status_post (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (faculty_id) REFERENCES faculty (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS news;

CREATE TABLE
    news (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        title TINYTEXT,
        summary TEXT,
        content TEXT,
        thumbnail TINYTEXT,
        faculty_id TINYINT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        published_at DATETIME,
        status_id TINYINT,
        views INT DEFAULT (0),
        children_comment_number INT DEFAULT (0),
        FOREIGN KEY (status_id) REFERENCES status_post (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (faculty_id) REFERENCES faculty (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag_post_advise;

CREATE TABLE
    tag_post_advise (
        post_advise_id VARCHAR(36) NOT NULL,
        tag_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        FOREIGN KEY (tag_id) REFERENCES tag (id),
        PRIMARY KEY (post_advise_id, tag_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag_post_group;

CREATE TABLE
    tag_post_group (
        post_group_id VARCHAR(36) NOT NULL,
        tag_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        FOREIGN KEY (tag_id) REFERENCES tag (id),
        PRIMARY KEY (post_group_id, tag_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag_event;

CREATE TABLE
    tag_event (
        event_id VARCHAR(36) NOT NULL,
        tag_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (event_id) REFERENCES event (id),
        FOREIGN KEY (tag_id) REFERENCES tag (id),
        PRIMARY KEY (event_id, tag_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag_news;

CREATE TABLE
    tag_news (
        news_id VARCHAR(36) NOT NULL,
        tag_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (news_id) REFERENCES news (id),
        FOREIGN KEY (tag_id) REFERENCES tag (id),
        PRIMARY KEY (news_id, tag_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS tag_group;

CREATE TABLE
    tag_group (
        group_id VARCHAR(36) NOT NULL,
        tag_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (group_id) REFERENCES `group` (id),
        FOREIGN KEY (tag_id) REFERENCES tag (id),
        PRIMARY KEY (group_id, tag_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS hall_of_fame;

CREATE TABLE
    hall_of_fame (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        title TINYTEXT,
        summary TEXT,
        content TEXT,
        thumbnail TINYTEXT,
        position TEXT,
        user_id VARCHAR(36),
        faculty_id TINYINT,
        beginning_year SMALLINT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        published_at DATETIME,
        status_id TINYINT,
        views INT DEFAULT (0),
        FOREIGN KEY (status_id) REFERENCES status_post (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (faculty_id) REFERENCES faculty (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS comment_post_advise;

CREATE TABLE
    comment_post_advise (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        post_advise_id VARCHAR(36) NOT NULL,
        parent_id VARCHAR(36),
        content TEXT,
        children_comment_number INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (parent_id) REFERENCES comment_post_advise (id),
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS comment_post_group;

CREATE TABLE
    comment_post_group (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        post_group_id VARCHAR(36) NOT NULL,
        parent_id VARCHAR(36),
        content TEXT,
        children_comment_number INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (parent_id) REFERENCES comment_post_group (id),
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS comment_news;

CREATE TABLE
    comment_news (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        news_id VARCHAR(36) NOT NULL,
        parent_id VARCHAR(36),
        content TEXT,
        children_comment_number INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (parent_id) REFERENCES comment_news (id),
        FOREIGN KEY (news_id) REFERENCES news (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS comment_event;

CREATE TABLE
    comment_event (
        id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        event_id VARCHAR(36) NOT NULL,
        parent_id VARCHAR(36),
        content TEXT,
        children_comment_number INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (parent_id) REFERENCES comment_event (id),
        FOREIGN KEY (event_id) REFERENCES event (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS react;

CREATE TABLE
    react (
        id TINYINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(50) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME,
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS interact_post_advise;

CREATE TABLE
    interact_post_advise (
        react_id TINYINT NOT NULL,
        post_advise_id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (react_id) REFERENCES react (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        PRIMARY KEY (post_advise_id, creator)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS interact_post_group;

CREATE TABLE
    interact_post_group (
        react_id TINYINT NOT NULL,
        post_group_id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (react_id) REFERENCES react (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        PRIMARY KEY (post_group_id, creator)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS interact_news;

CREATE TABLE
    interact_news (
        react_id TINYINT NOT NULL,
        news_id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (react_id) REFERENCES react (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (news_id) REFERENCES news (id),
        PRIMARY KEY (news_id, creator)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS interact_event;

CREATE TABLE
    interact_event (
        react_id TINYINT NOT NULL,
        event_id VARCHAR(36) NOT NULL,
        creator VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (react_id) REFERENCES react (id),
        FOREIGN KEY (creator) REFERENCES user (id),
        FOREIGN KEY (event_id) REFERENCES event (id),
        PRIMARY KEY (event_id, creator)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS participant_event;

CREATE TABLE
    participant_event (
        event_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        note TEXT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (user_id) REFERENCES user (id),
        FOREIGN KEY (event_id) REFERENCES event (id),
        PRIMARY KEY (event_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS vote_option_post_advise;

CREATE TABLE
    vote_option_post_advise (
        id TINYINT NOT NULL,
        post_advise_id VARCHAR(36) NOT NULL,
        name VARCHAR(150) NOT NULL,
        vote_count INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        PRIMARY KEY (id, post_advise_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS user_vote_post_advise;

CREATE TABLE
    user_vote_post_advise (
        vote_id TINYINT NOT NULL,
        post_advise_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (vote_id) REFERENCES vote_option_post_advise (id),
        FOREIGN KEY (post_advise_id) REFERENCES post_advise (id),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (vote_id, post_advise_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS vote_option_post_group;

CREATE TABLE
    vote_option_post_group (
        id TINYINT NOT NULL,
        post_group_id VARCHAR(36) NOT NULL,
        name VARCHAR(150) NOT NULL,
        vote_count INT DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        PRIMARY KEY (id, post_group_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS user_vote_post_group;

CREATE TABLE
    user_vote_post_group (
        vote_id TINYINT NOT NULL,
        post_group_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (vote_id) REFERENCES vote_option_post_group (id),
        FOREIGN KEY (post_group_id) REFERENCES post_group (id),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (vote_id, post_group_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

BEGIN;

DROP TABLE IF EXISTS inbox;

CREATE TABLE
    inbox (
        id VARCHAR(36) NOT NULL,
        name TINYTEXT, -- Name of the group (only applicable if it's a group chat).
        is_group TINYINT (1) DEFAULT (0),
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

BEGIN;

DROP TABLE IF EXISTS inbox_member;

CREATE TABLE
    inbox_member (
        inbox_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        role ENUM ('ADMIN', 'MEMBER') NOT NULL,
        joined_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (inbox_id) REFERENCES inbox (id),
        FOREIGN KEY (user_id) REFERENCES user (id),
        PRIMARY KEY (inbox_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

BEGIN;

DROP TABLE IF EXISTS message;

CREATE TABLE
    message (
        id BIGINT NOT NULL AUTO_INCREMENT,
        inbox_id VARCHAR(36) NOT NULL,
        sender_id VARCHAR(36) NOT NULL,
        content TEXT,
        message_type ENUM ('TEXT', 'IMAGE', 'FILE', 'VIDEO', 'SOUND') NOT NULL,
        parent_message_id BIGINT,
        create_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
        update_at DATETIME DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
        is_delete TINYINT (1) DEFAULT (0),
        FOREIGN KEY (inbox_id) REFERENCES inbox (id),
        FOREIGN KEY (sender_id) REFERENCES user (id),
        FOREIGN KEY (parent_message_id) REFERENCES message (id),
        PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

BEGIN;

DROP TABLE IF EXISTS password_history;

CREATE TABLE
    password_history (
        `id` varchar(36) CHARACTER
        SET
            utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
            `user_id` varchar(36) CHARACTER
        SET
            utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
            `password` varchar(60) CHARACTER
        SET
            utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
            `is_auto_generated` tinyint (1) NOT NULL DEFAULT '0',
            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (`id`),
            KEY `user_id` (`user_id`),
            CONSTRAINT `passwo_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- INDEX
ALTER TABLE `group` ADD INDEX idx_creator (creator);

ALTER TABLE post_group ADD INDEX idx_creator (creator);

ALTER TABLE post_advise ADD INDEX idx_creator (creator);

ALTER TABLE event ADD INDEX idx_creator (creator);

ALTER TABLE news ADD INDEX idx_creator (creator);

ALTER TABLE hall_of_fame ADD INDEX idx_creator (creator);

ALTER TABLE comment_post_advise ADD INDEX idx_creator (creator);

ALTER TABLE comment_post_group ADD INDEX idx_creator (creator);

ALTER TABLE comment_news ADD INDEX idx_creator (creator);

ALTER TABLE event ADD INDEX idx_create_at (create_at);

ALTER TABLE news ADD INDEX idx_create_at (create_at);

ALTER TABLE hall_of_fame ADD INDEX idx_create_at (create_at);

-- INSERT DATA
INSERT INTO
    permission (name, description)
VALUES
    ('User.Create', 'Tạo tài khoản'),
    (
        'User.Edit',
        'Chỉnh sửa tài khoản (gồm khoá tài khoản)'
    ),
    ('User.Delete', 'Xóa tài khoản'),
    ('User.Role.Create', 'Tạo vai trò mới'),
    (
        'User.Role.Edit',
        'Chỉnh sửa vai trò. Phân quyền (gán các quyền cụ thể cho từng vai trò)'
    ),
    ('User.Role.Delete', 'Xoá vai trò'),
    (
        'AlumniVerify.Read',
        'Xem danh sách xác thực cựu sinh viên'
    ),
    ('AlumniVerify.Edit', 'Phê duyệt cựu sinh viên'),
    (
        'AlumniVerify.Create',
        'Gửi xác thực cựu sinh viên'
    ),
    ('News.Create', 'Tạo/Lên lịch đăng tin tức'),
    ('News.Edit', 'Chỉnh sức tin tức (Bao gồm ẩn)'),
    ('News.Delete', 'Xóa tin tức'),
    ('News.Comment.Create', 'Viết bình luận'),
    (
        'News.Comment.Delete',
        'Xoá bình luận của tất cả mọi người'
    ),
    ('Event.Create', 'Tạo sự kiện'),
    ('Event.Edit', 'Chỉnh sửa sự kiện (Bao gồm ẩn)'),
    ('Event.Delete', 'Xóa sự kiện'),
    ('Event.Participant.Create', 'Tham gia sự kiện'),
    (
        'Event.Participant.Delete',
        'Hủy tham gia sự kiện'
    ),
    ('Event.Comment.Create', 'Viết bình luận'),
    (
        'Event.Comment.Delete',
        'Xoá bình luận của tất cả mọi người'
    ),
    ('Hof.Create', 'Tạo bài viết Gương thành công'),
    (
        'Hof.Edit',
        'Chỉnh sửa bài viết Gương thành công (Bao gồm ẩn)'
    ),
    ('Hof.Delete', 'Xóa bài viết Gương thành công'),
    ('Counsel.Create', 'Tạo bài viết tư vấn/cố vấn'),
    (
        'Counsel.Delete',
        'Xóa bài viết tư vấn/cố vấn của tất cả mọi người'
    ),
    ('Counsel.Reaction.Create', 'Thả cảm xúc bài viết'),
    ('Counsel.Comment.Create', 'Viết bình luận'),
    (
        'Counsel.Comment.Delete',
        'Xoá bình luận của tất cả mọi người trên tất cả các post'
    ),
    (
        'Counsel.Vote',
        'Bình chọn cuộc thăm dò ý kiến trong bài viết (gồm tạo, sửa và xóa)'
    ),
    ('Group.Create', 'Tạo nhóm'),
    ('Group.Delete', 'Xóa nhóm'),
    ('Group.Join', 'Tham gia nhóm'),
    (
        'Profile.Edit',
        'Chỉnh sửa thông tin cá nhân/Thay đổi mật khẩu'
    ),
    ('Message.Create', 'Gửi/Nhận tin nhắn');

INSERT into
    role (name)
VALUES
    ('Admin'),
    ('FacultyManager'),
    ('Lecturer'),
    ('Alumni'),
    ('Guest');

INSERT INTO
    `role_permission`
VALUES
    (1, 1, 0),
    (1, 2, 0),
    (1, 3, 0),
    (1, 4, 0),
    (1, 5, 0),
    (1, 6, 0),
    (1, 7, 0),
    (1, 8, 0),
    (1, 10, 0),
    (1, 11, 0),
    (1, 12, 0),
    (1, 14, 0),
    (1, 15, 0),
    (1, 16, 0),
    (1, 17, 0),
    (1, 21, 0),
    (1, 22, 0),
    (1, 23, 0),
    (1, 24, 0),
    (1, 26, 0),
    (1, 29, 0),
    (1, 30, 0),
    (1, 31, 0),
    (1, 32, 0),
    (1, 33, 0);

INSERT into
    sex (name)
VALUES
    ('Nam'),
    ('Nữ');

INSERT into
    faculty (name)
VALUES
    ('Công nghệ thông tin'),
    ('Vật lý – Vật lý kỹ thuật'),
    ('Địa chất'),
    ('Toán – Tin học'),
    ('Điện tử - Viễn thông'),
    ('Khoa học & Công nghệ Vật liệu'),
    ('Hóa học'),
    ('Sinh học – Công nghệ Sinh học'),
    ('Môi trường');

INSERT into
    status_user_group (name)
VALUES
    ('Khoá'),
    ('Bình thường'),
    ('Xoá');

INSERT into
    status_post (name)
VALUES
    ('Chờ'),
    ('Bình thường'),
    ('Ẩn'),
    ('Xoá');

INSERT into
    react (name)
VALUES
    ('Like');