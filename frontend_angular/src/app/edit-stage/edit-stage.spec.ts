import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditStage } from './edit-stage';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('EditStage', () => {
  let component: EditStage;
  let fixture: ComponentFixture<EditStage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [EditStage],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(EditStage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
