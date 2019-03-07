package org.camunda.bpm.getstarted.pizza;

import org.camunda.bpm.engine.cdi.jsf.TaskForm;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Named
public class OrderBusinessLogic implements Serializable {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TaskForm taskForm;

    private static Logger LOGGER = Logger.getLogger(OrderBusinessLogic.class.getName());


    public void persistOrder(DelegateExecution delegateExecution){
        OrderEntity orderEntity = new OrderEntity();

        Map<String, Object> variables = delegateExecution.getVariables();
        orderEntity.setCustomer((String)variables.get("customer"));
        orderEntity.setAddress((String)variables.get("address"));
        orderEntity.setPizza((String)variables.get("pizza"));
        entityManager.persist(orderEntity);
        entityManager.flush();

        delegateExecution.removeVariables(variables.keySet());

        delegateExecution.setVariable("orderId", orderEntity.getId());
    }

    public OrderEntity getOrder(Long orderId) {
        // Load order entity from database
        return entityManager.find(OrderEntity.class, orderId);
    }

    public void mergeOrderAndCompleteTask(OrderEntity orderEntity) {
        // Merge detached order entity with current persisted state
        entityManager.merge(orderEntity);
        try {
            // Complete user task from
            taskForm.completeTask();
        } catch (IOException e) {
            // Rollback both transactions on error
            throw new RuntimeException("Cannot complete task", e);
        }
    }

    public void rejectOrder(DelegateExecution delegateExecution) {
        OrderEntity order = getOrder((Long) delegateExecution.getVariable("orderId"));
        LOGGER.log(Level.INFO, "\n\n\nSending Email:\nDear {0}, your order {1} of a {2} pizza has been rejected.\n\n\n", new String[]{order.getCustomer(), String.valueOf(order.getId()), order.getPizza()});
    }
}
