package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.models.domain.model.TransferError

class ModelTransferFailure(val error: TransferError, cause: Throwable? = null) : Exception(error.name, cause)
