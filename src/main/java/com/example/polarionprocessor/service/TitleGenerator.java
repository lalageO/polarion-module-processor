package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleProcessRequest;

public interface TitleGenerator {

    String generate(ImportItemResult item, ModuleProcessRequest request);
}
