package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;

public interface CompensationService {
	public Compensation create(Compensation compensation);
	public Compensation read(String employeeId);
}
