package com.fidelity.mts.service;

import com.fidelity.mts.application.dto.TransferRequest;
import com.fidelity.mts.application.dto.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}
