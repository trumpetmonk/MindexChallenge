package com.mindex.challenge.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.ReportingStructureService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceTest {
	private String employeeUrl;
	private String employeeIdUrl;
	private String reportingStructureIdUrl;

	@Autowired
	private ReportingStructureService reportingStructureService;
	@Autowired
	private EmployeeService employeeService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private Employee nickFury;
	private Employee captainAmerica;

	private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceTest.class);

	@Before
	public void setup() {
		employeeUrl = "http://localhost:" + port + "/employee";
		employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
		reportingStructureIdUrl = "http://localhost:" + port + "/reportingStructure/{id}";

		// setup a couple employees
		nickFury = new Employee();
		nickFury.setFirstName("Nick");
		nickFury.setLastName("Fury");
		nickFury.setDepartment("Operations");
		nickFury.setPosition("Operations Manager");
		Employee furyIdOnly = new Employee();

		captainAmerica = new Employee();
		captainAmerica.setFirstName("Steve");
		captainAmerica.setLastName("Rodgers");
		captainAmerica.setDepartment("Engineering");
		captainAmerica.setPosition("Development Manager");
		captainAmerica.setDirectReports(new LinkedList<Employee>());
		Employee capIdOnly = new Employee();

		Employee ironMan = new Employee();
		ironMan.setFirstName("Tony");
		ironMan.setLastName("Stark");
		ironMan.setDepartment("Engineering");
		ironMan.setPosition("Developer III");
		Employee ironManIdOnly = new Employee();

		Employee hulk = new Employee();
		hulk.setFirstName("Bruce");
		hulk.setLastName("Banner");
		hulk.setDepartment("Engineering");
		hulk.setPosition("Developer II");
		Employee hulkIdOnly = new Employee();

		Employee thor = new Employee();
		thor.setFirstName("Thor");
		thor.setLastName("Odinson");
		thor.setDepartment("Engineering");
		thor.setPosition("Developer I");
		Employee thorIdOnly = new Employee();

		// get the employee ids from the system
		nickFury = restTemplate.postForEntity(employeeUrl, nickFury, Employee.class).getBody();
		captainAmerica = restTemplate.postForEntity(employeeUrl, captainAmerica, Employee.class)
				.getBody();
		ironMan = restTemplate.postForEntity(employeeUrl, ironMan, Employee.class).getBody();
		hulk = restTemplate.postForEntity(employeeUrl, hulk, Employee.class).getBody();
		thor = restTemplate.postForEntity(employeeUrl, thor, Employee.class).getBody();

		// update the ids of the id only employees for storing as direct reports
		furyIdOnly.setEmployeeId(nickFury.getEmployeeId());
		capIdOnly.setEmployeeId(captainAmerica.getEmployeeId());
		ironManIdOnly.setEmployeeId(ironMan.getEmployeeId());
		thorIdOnly.setEmployeeId(thor.getEmployeeId());
		hulkIdOnly.setEmployeeId(hulk.getEmployeeId());

		// read the database
		Employee readCap = restTemplate
				.getForEntity(employeeIdUrl, Employee.class, captainAmerica.getEmployeeId())
				.getBody();
		Employee readNick = restTemplate
				.getForEntity(employeeIdUrl, Employee.class, nickFury.getEmployeeId())

				.getBody();
		// update the direct reports
		List<Employee> furyReports = new LinkedList<Employee>();
		furyReports.add(capIdOnly);
		readNick.setDirectReports(furyReports);

		List<Employee> capReports = new LinkedList<Employee>();
		capReports.add(ironManIdOnly);
		capReports.add(thorIdOnly);
		capReports.add(hulkIdOnly);
		readCap.setDirectReports(capReports);

		LOG.info(readCap.getDirectReports().size() + " ");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		nickFury = restTemplate.exchange(employeeIdUrl, HttpMethod.PUT,
				new HttpEntity<Employee>(readNick, headers), Employee.class,
				readNick.getEmployeeId()).getBody();
		captainAmerica = restTemplate.exchange(employeeIdUrl, HttpMethod.PUT,
				new HttpEntity<Employee>(readCap, headers), Employee.class, readCap.getEmployeeId())
				.getBody();

	}

	@Test
	public void testGet() {

		// Run the reporting structure
		ReportingStructure capStructure = restTemplate.getForEntity(reportingStructureIdUrl,
				ReportingStructure.class, captainAmerica.getEmployeeId()).getBody();
		assertNotNull(capStructure);
		assertEquals(capStructure.getNumberOfReports(), 3);

		ReportingStructure nickStructure = restTemplate.getForEntity(reportingStructureIdUrl,
				ReportingStructure.class, nickFury.getEmployeeId()).getBody();
		assertNotNull(nickStructure);
		assertEquals(nickStructure.getNumberOfReports(), 4);
	}
}
