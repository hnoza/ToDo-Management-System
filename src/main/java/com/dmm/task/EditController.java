package com.dmm.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;

@Controller
public class EditController {

	// @Autowiredアノテーションを付けると、Spring Bootが自動でインスタンスをInjectします。
	@Autowired
	private TasksRepository repo;
	
	/**
	 * 編集画面表示.
	 * 
	 * @param id タスクID
	 * @param model モデル
	 * @return 遷移先
	 */
	@GetMapping("/main/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		
		Tasks task = repo.getById(id);
		model.addAttribute("task", task);
		
		return "edit"; // edit.html
	}
	
	/**
	 * 更新処理.
	 * 
	 * @param taskForm 登録フォーム
	 * @param id タスクID
	 * @return 遷移先
	 */
	@PostMapping("/main/edit/{id}")
	public String regster(@Validated TaskForm taskForm, @PathVariable Integer id) {
		
		// 更新前のタスク情報取得
		Tasks oldtask = repo.getById(id);
		
		Tasks task = new Tasks();
		task.setId(id);
		task.setTitle(taskForm.getTitle());
		task.setName(oldtask.getName()); // タスクの登録ユーザは変更しない
		task.setText(taskForm.getText());
		task.setDate(LocalDate.parse(taskForm.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		
		// 未完了のときはnullになっている
		if (taskForm.getDone() != null ) {
			task.setDone(taskForm.getDone());
		} else {
			task.setDone(false);
		}
		
		repo.save(task);

		return "redirect:/main";
	}
	
	/**
	 * 削除処理.
	 * 
	 * @param id タスクID
	 * @return 遷移先
	 */
	@PostMapping("/main/delete/{id}")
	public String delete(@PathVariable Integer id) {
		
		repo.deleteById(id);

		return "redirect:/main";
	}
}
