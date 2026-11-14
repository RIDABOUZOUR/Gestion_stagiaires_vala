import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewStage } from './new-stage';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('NewStage', () => {
  let component: NewStage;
  let fixture: ComponentFixture<NewStage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [NewStage],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(NewStage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
