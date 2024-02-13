package se.sodalabs.demo.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.ContentAlignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.sodalabs.demo.domain.FeedbackDTO;
import se.sodalabs.demo.service.HubService;

@PageTitle("Feedback Survey")
@Route(value = "/")
public class ParticipantDashboardView extends VerticalLayout {

  FlexLayout dashboardLayout = new FlexLayout();

  @Autowired
  public ParticipantDashboardView(
      HubService hubService,
      @Value("${service.tag}") String id,
      @Value("${service.name}") String name) {

    setHeightFull();
    setWidthFull();
    setAlignItems(Alignment.CENTER);
    addClassName("participant-dashboard-view");

    dashboardLayout.addClassName("dashboard-column");
    dashboardLayout.setAlignContent(ContentAlignment.CENTER);
    dashboardLayout.setFlexDirection(FlexDirection.COLUMN);

    H2 title = new H2(name);
    title.addClassName("name");
    title.setWidthFull();

    Div serviceId = new Div();
    serviceId.addClassName("id");
    serviceId.setWidthFull();
    serviceId.setText(id);

    Button registerButton =
        new Button(
            "Register",
            event -> {
              String response = String.valueOf(hubService.registerNewParticipant().getStatusCode());
              showResponse(response);
            });
    registerButton.setWidthFull();
    registerButton.addClassName("button");

    Button unregisterButton =
        new Button(
            "Unregister",
            event -> {
              String response = String.valueOf(hubService.unregisterParticipant().getStatusCode());
              showResponse(response);
            });
    registerButton.setWidthFull();
    registerButton.addClassName("button");

    Div firstExerciseFeedbackDiv = getFirstExerciseFeedbackDiv(hubService);
    Div secondExerciseFeedbackDiv = getSecondExerciseFeedbackDiv(hubService);

    dashboardLayout.add(
        title,
        serviceId,
        registerButton,
        unregisterButton,
        firstExerciseFeedbackDiv,
        secondExerciseFeedbackDiv);
    add(dashboardLayout);
  }

  @NotNull
  private static Div getFirstExerciseFeedbackDiv(HubService hubService) {
    Div firstExerciseFeedbackDiv = new Div();
    firstExerciseFeedbackDiv.setWidthFull();
    firstExerciseFeedbackDiv.addClassName("exercise-container");
    RadioButtonGroup<Integer> radioButtonGroup = new RadioButtonGroup<>();
    radioButtonGroup.setLabel("First Exercise Happiness Score:");
    radioButtonGroup.setItems(0, 25, 50, 75, 100);
    radioButtonGroup.setWidthFull();
    radioButtonGroup.addClassName("radio-button-group");

    Button submitButton =
        new Button(
            "Submit",
            event -> {
              Integer selectedValue = radioButtonGroup.getValue();
              if (selectedValue != null) {
                Notification.show(
                    String.valueOf(hubService.sendFeedback(selectedValue).getStatusCode()));
              } else {
                Notification.show("Please select a happiness score!");
              }
            });
    submitButton.setWidthFull();
    submitButton.addClassName("button");
    firstExerciseFeedbackDiv.add(radioButtonGroup, submitButton);
    return firstExerciseFeedbackDiv;
  }

  @NotNull
  private static Div getSecondExerciseFeedbackDiv(HubService hubService) {
    Div secondExerciseFeedbackDiv = new Div();
    secondExerciseFeedbackDiv.setWidthFull();
    secondExerciseFeedbackDiv.addClassName("exercise-container");
    RadioButtonGroup<Integer> radioButtonGroup = new RadioButtonGroup<>();
    radioButtonGroup.setLabel("Second Exercise Happiness Score:");
    radioButtonGroup.setItems(0, 25, 50, 75, 100);
    radioButtonGroup.setWidthFull();
    radioButtonGroup.addClassName("radio-button-group");

    Input commentInput = new Input();
    commentInput.setType("text");
    commentInput.setWidthFull();
    commentInput.setPlaceholder("Comment");

    Button submitButton =
        new Button(
            "Submit",
            event -> {
              Integer selectedValue = radioButtonGroup.getValue();
              if (selectedValue != null) {
                FeedbackDTO feedbackToSubmit = new FeedbackDTO(1, selectedValue, "");
                Notification.show(
                    String.valueOf(hubService.sendFeedback(feedbackToSubmit).getStatusCode()));
              } else {
                Notification.show("Please select a happiness score!");
              }
            });
    submitButton.setWidthFull();
    submitButton.addClassName("button");
    secondExerciseFeedbackDiv.add(radioButtonGroup, commentInput, submitButton);
    return secondExerciseFeedbackDiv;
  }

  private void showResponse(String response) {
    Notification.show("Response: " + response);
  }
}
