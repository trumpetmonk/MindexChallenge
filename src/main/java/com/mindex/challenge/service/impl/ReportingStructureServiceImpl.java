package com.mindex.challenge.service.impl;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

	private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	@Override
	public ReportingStructure getReportingStructure(String employeeId) {
		LOG.debug("Getting ReportingStructure for employeeId:[{}]", employeeId);

		for (Employee employee : employeeRepository.findAll()) {
			LOG.info(employee + "   first name: " + employee.getFirstName() + "id: "
					+ employee.getEmployeeId());
		}
		Employee employee = employeeRepository.findByEmployeeId(employeeId);
		if (employee == null) {
			throw new RuntimeException("Invalid employeeId: " + employeeId);
		}

		ReportingStructure reportingStructure = new ReportingStructure();
		reportingStructure.setEmployee(employee);
		reportingStructure.setNumberOfReports(calculateNumberOfReports(employee));
		return reportingStructure;
	}

	private int calculateNumberOfReports(Employee employee) {
		Stack<Employee> searchStack = new Stack<Employee>();
		int numberOfReports = 0;

		if (employeeHasDirectReports(employee)) {
			for (Employee report : employee.getDirectReports()) {
				searchStack.push(report);
			}

			while (!searchStack.isEmpty()) {
				Employee currentEmployee = searchStack.pop();
				currentEmployee = employeeRepository
						.findByEmployeeId(currentEmployee.getEmployeeId());
				numberOfReports++;
				if (employeeHasDirectReports(currentEmployee)) {
					for (Employee report : currentEmployee.getDirectReports()) {
						searchStack.push(report);
					}
				}
			}
		}
		return numberOfReports;
	}

	private boolean employeeHasDirectReports(Employee employee) {
		return employee.getDirectReports() != null;
	}
}
