package com.microhabits.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class FocusViewModel_Factory implements Factory<FocusViewModel> {
  @Override
  public FocusViewModel get() {
    return newInstance();
  }

  public static FocusViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FocusViewModel newInstance() {
    return new FocusViewModel();
  }

  private static final class InstanceHolder {
    private static final FocusViewModel_Factory INSTANCE = new FocusViewModel_Factory();
  }
}
