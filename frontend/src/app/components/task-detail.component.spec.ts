import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskDetailComponent } from './task-detail.component';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

describe('TaskDetailComponent', () =>
{
  let fixture: ComponentFixture<TaskDetailComponent>;
  let component: TaskDetailComponent;

  const apiSpy = jasmine.createSpyObj<ApiService>('ApiService', [
    'task', 'updateTask', 'taskHistory', 'assignTask', 'addTaskHistory', 'me', 'members'
  ]);

  beforeEach(async () =>
  {
    apiSpy.task.and.returnValue(Promise.resolve({
      id: 123,
      name: 'T1',
      description: 'D',
      dueDate: '2025-08-01',
      priority: 'LOW',
      status: 'TODO',
      project: { id: 7 }
    }));

    apiSpy.me.and.returnValue(Promise.resolve({ role: 'ADMIN' }));
    apiSpy.members.and.returnValue(Promise.resolve([]));
    apiSpy.taskHistory.and.returnValue(Promise.resolve([]));
    apiSpy.updateTask.and.returnValue(Promise.resolve());
    apiSpy.assignTask.and.returnValue(Promise.resolve());
    apiSpy.addTaskHistory.and.returnValue(Promise.resolve({}));

    await TestBed.configureTestingModule({
      declarations: [TaskDetailComponent],
      imports: [CommonModule, FormsModule, RouterTestingModule],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        { provide: SessionService, useValue: { userId: 1 } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '123' }) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should call updateTask when setStatus(DONE)', async () =>
  {
    await component.setStatus('DONE');

    expect(apiSpy.updateTask).toHaveBeenCalled();
  });

  it('should call updateTask on save (with completedAt formatting)', async () =>
  {
    (component as any).role = 'ADMIN';
    component.task = { id: 123, name: 'T1', description: 'D' };
    (component as any).form = {
      name: 'N2',
      description: 'D2',
      dueDate: '2025-08-15',
      priority: 'HIGH',
      completedAt: '2025-08-20'
    };

    await component.save();

    expect(apiSpy.updateTask).toHaveBeenCalled();
    const args = apiSpy.updateTask.calls.mostRecent().args;
    expect(args[2].completedAt).toContain('2025-08-20T00:00:00.000Z');
  });

  it('should call assignTask when assign() with rights and assigneeId', async () =>
  {
    (component as any).role = 'ADMIN';
    (component as any).assigneeId = 2;
    component.task = { id: 123 };

    await component.assign();

    expect(apiSpy.assignTask).toHaveBeenCalledWith(123, 2);
  });

  it('renderDiff should format field change and text fallback', () =>
  {
    const withField = component.renderDiff({
      changedField: 'priority',
      oldValue: 'LOW',
      newValue: 'HIGH'
    });

    expect(withField).toContain('priorité');
    expect(withField).toContain('LOW');
    expect(withField).toContain('HIGH');

    const withText = component.renderDiff({ text: 'note libre' });
    expect(withText).toBe('note libre');
  });

  it('renderWhen should format ISO dates', () =>
  {
    const s = component.renderWhen({ at: '2025-08-12T10:00:00Z' });

    expect(typeof s).toBe('string');
    expect(s.length).toBeGreaterThan(5);
  });

});
