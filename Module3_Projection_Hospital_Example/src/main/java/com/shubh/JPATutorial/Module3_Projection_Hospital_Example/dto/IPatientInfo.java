package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto;


// created a dto: used in repository to fetch selective information


public interface IPatientInfo {

    Long getId();

    String getName();

    String getEmail();
}
