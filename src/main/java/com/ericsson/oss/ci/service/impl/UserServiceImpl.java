/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.ci.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ericsson.oss.ci.domain.entity.UserEntity;
import com.ericsson.oss.ci.domain.repository.UserRepository;
import com.ericsson.oss.ci.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

	@Override
	public void saveUser(UserEntity userEntity) {
		log.info("Saved the new user successfully");
		userRepository.save(userEntity);
	}

	@Override
	public Long userCount() {
		return userRepository.countDistinctUsersCreatedToday();
	}
	
	@Override
	public List<Object[]> countDistinctUsersUntilToday() {
        return userRepository.countDistinctUsersByDayUntilDate(LocalDate.now().plusDays(1));
    }
}
