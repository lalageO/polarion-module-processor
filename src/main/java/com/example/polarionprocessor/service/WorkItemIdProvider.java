package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ImportItemResult;

public interface WorkItemIdProvider {

    String provide(ImportItemResult item, int index);
}
