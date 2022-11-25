package com.dmm.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class CreateController {

	// @Autowiredアノテーションを付けると、Spring Bootが自動でインスタンスをInjectします。
	@Autowired
	private TasksRepository repo;
	
	/**
	 * 登録画面表示.
	 * 
	 * @param dateStr 日付文字列（YYYY-MM-DD形式）
	 * @param model モデル
	 * @return 遷移先
	 */
	@GetMapping("/main/create/{dateStr}")
	public String create(@PathVariable String dateStr, Model model) {
		
		TaskForm taskForm = new TaskForm();
		taskForm.setDate(dateStr);
		model.addAttribute("taskForm", taskForm);
		
		return "create"; // create.html
	}
	
	/**
	 * 登録処理.
	 * 
	 * @param taskForm 登録フォーム
	 * @param user ユーザ情報
	 * @return 遷移先
	 */
	@PostMapping("/main/create/register")
	public String regster(@Validated TaskForm taskForm, @AuthenticationPrincipal AccountUserDetails user) {
		
		Tasks task = new Tasks();
		task.setTitle(taskForm.getTitle());
		task.setName(user.getName());
		task.setText(taskForm.getText());
		task.setDate(LocalDate.parse(taskForm.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		task.setDone(false); // 登録時なのでfalseをセット
		
		repo.save(task);

		return "redirect:/main";
	}
	
}
