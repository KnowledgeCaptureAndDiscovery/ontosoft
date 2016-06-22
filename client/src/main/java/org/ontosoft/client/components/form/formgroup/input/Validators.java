package org.ontosoft.client.components.form.formgroup.input;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.regexp.shared.RegExp;

public class Validators {

  public static Validator<String> NO_BLANK_STRINGS = new Validator<String>() {
    @Override
    public List<EditorError> validate(Editor<String> editor, String value) {
      List<EditorError> result = new ArrayList<EditorError>();
      if (value == null || value.equals(""))
        result.add(new BasicEditorError(editor, value, null));
      return result;
    }
    @Override
    public int getPriority() {
      return Priority.LOW;
    }
  };
  
  public static Validator<Double> DOUBLE = new Validator<Double>() {
    @Override
    public List<EditorError> validate(Editor<Double> editor, Double value) {
      List<EditorError> result = new ArrayList<EditorError>();
      if (value == null)
        result.add(new BasicEditorError(editor, value, "Not a valid number"));
      return result;
    }

    @Override
    public int getPriority() {
      return Priority.MEDIUM;
    }
  };

  public static Validator<String> URL = new Validator<String>() {
    @Override
    public List<EditorError> validate(Editor<String> editor, String value) {
      List<EditorError> result = new ArrayList<EditorError>();
      if (value != null && !isValidUrl(value, true)) {
        result.add(new BasicEditorError(editor, value, "Not a valid url"));
      }
      return result;
    }

    @Override
    public int getPriority() {
      return Priority.MEDIUM;
    }
  };

  // Helper functions;
  private static RegExp urlValidator;
  private static RegExp urlPlusTldValidator;
  private static boolean isValidUrl(String url, boolean topLevelDomainRequired) {
    if (urlValidator == null || urlPlusTldValidator == null) {
      urlValidator = RegExp
          .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!~\\_\\-/]+)*){1}$");
      urlPlusTldValidator = RegExp
          .compile("^((ftp|http|https)://[\\w@.\\-\\_]+\\.[a-zA-Z]{2,}(:\\d{1,5})?(/[\\w#!:.?+=&%@!~\\_\\-/]+)*){1}$");
    }
    return (topLevelDomainRequired ? urlPlusTldValidator : urlValidator)
        .exec(url) != null;
  }

}
