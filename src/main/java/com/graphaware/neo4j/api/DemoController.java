package com.graphaware.neo4j.api;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("demo")
public class DemoController {

    private final GraphDatabaseService database;

    @Autowired
    public DemoController(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Integer> getSomeIntegers() {
        return Arrays.asList(1, 2, 3);
    }

}
