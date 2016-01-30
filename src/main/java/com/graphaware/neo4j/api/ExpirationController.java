package com.graphaware.neo4j.api;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("expire")
public class ExpirationController {

    private final GraphDatabaseService database;

    @Autowired
    public ExpirationController(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value="/from/{timestamp}", method = RequestMethod.POST)
    @ResponseBody
    public void expiresBy(Long timestamp) {

    }

}
