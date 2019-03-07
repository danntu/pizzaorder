package org.camunda.bpm.getstarted.pizza;

import org.camunda.bpm.engine.cdi.BusinessProcess;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.Serializable;

@Named
@ConversationScoped
public class ApproveOrderController implements Serializable {
    private static final long serialVersionUID= 1L;

    @Inject
    private BusinessProcess businessProcess;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private OrderBusinessLogic orderBusinessLogic;

    private OrderEntity orderEntity;


    public OrderEntity getOrderEntity() {
        if (orderEntity == null) {
            // Load the order entity from the database if not already cached
            orderEntity = orderBusinessLogic.getOrder((Long) businessProcess.getVariable("orderId"));
        }
        return orderEntity;
    }

    public void submitForm() throws IOException {
        // Persist updated order entity and complete task form
        orderBusinessLogic.mergeOrderAndCompleteTask(orderEntity);
    }

}
