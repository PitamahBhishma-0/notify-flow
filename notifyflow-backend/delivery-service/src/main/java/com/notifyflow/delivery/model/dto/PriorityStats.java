package com.notifyflow.delivery.model.dto;

import com.notiflyflow.notifycommon.dto.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityStats {
    private Priority priority;
    private long sent;
    private long delivered;
    private long failed;
}
