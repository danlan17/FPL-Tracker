package com.dan.fpl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.dan.fpl.services.FPLService;

@RestController
public class FPLServiceController {

	@Autowired
	private FPLService service;
	
	
}
